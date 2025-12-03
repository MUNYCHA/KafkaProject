package org.munycha.kafkaconsumer.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.munycha.kafkaconsumer.telegram.TelegramNotifier;
import org.munycha.kafkaconsumer.db.AlertDatabase;
import org.munycha.kafkaconsumer.utility.KafkaTopicValidator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class TopicConsumer implements Runnable {

    private final String topic;
    private final KafkaConsumer<String, String> consumer;
    private final Path outputFile;
    private final TelegramNotifier notifier;
    private final List<String> alertKeywords;
    private final AlertDatabase db;

    public TopicConsumer(String bootstrapServers,
                         String topic,
                         String outputFilePath,
                         String botToken,
                         String chatId,
                         List<String> alertKeywords,
                         AlertDatabase db) {

        this.topic = topic;
        this.alertKeywords = alertKeywords;
        this.db = db;

        this.consumer = createConsumer(bootstrapServers, topic);
        this.outputFile = Path.of(outputFilePath);
        this.notifier = new TelegramNotifier(botToken, chatId);
    }

    // ------------------------------------------------------------
    // 1. Create Kafka consumer
    // ------------------------------------------------------------
    private KafkaConsumer<String, String> createConsumer(String bootstrapServers, String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "file-log-consumer-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        if (!KafkaTopicValidator.topicExists(topic, props)) {
            System.err.println("[Kafka] Topic does NOT exist: " + topic);
            throw new RuntimeException("Kafka topic does not exist: " + topic);
        }

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    // ------------------------------------------------------------
    // 2. Ensure output file exists
    // ------------------------------------------------------------
    private void ensureOutputFileExists() {
        if (!Files.exists(outputFile)) {
            System.err.println(" Output file does NOT exist: " + outputFile);
            System.err.println("   Please create it manually. Consumer will NOT write.");
        }
    }

    // ------------------------------------------------------------
    // 3. Main consumer loop
    // ------------------------------------------------------------
    @Override
    public void run() {

        ensureOutputFileExists();

        try (FileWriter writer = new FileWriter(outputFile.toFile(), true)) {

            System.out.printf("Listening to %s → writing to %s%n", topic, outputFile);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, String> record : records) {
                    handleRecord(record, writer);
                }
            }

        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        } finally {
            consumer.close();
        }
    }

    // ------------------------------------------------------------
    // 4. Handle record
    // ------------------------------------------------------------
    private void handleRecord(ConsumerRecord<String, String> record, FileWriter writer) throws IOException {
        String msg = record.value();
        String lower = msg.toLowerCase();

        System.out.printf("[%s] (%s) %s%n",
                java.time.LocalTime.now(), record.topic(), msg);


        if (Files.exists(outputFile)) {
            writer.write(msg + System.lineSeparator());
            writer.flush();
        }

        boolean alert = alertKeywords.stream().anyMatch(lower::contains);

        if (alert) {
            processAlert(record.topic(), msg);
        }
    }

    // ------------------------------------------------------------
    // 5. Process alert (Telegram + DB insert)
    // ------------------------------------------------------------
    private void processAlert(String topic, String msg) {

        LocalDateTime timestamp = LocalDateTime.now();
        String displayTimestamp = timestamp.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String alertMessage =
                "ALERT\n" +
                        " Timestamp: " + displayTimestamp + "\n" +
                        " Topic: " + topic + "\n" +
                        " Message: " + msg;

        // Telegram async send
        new Thread(() -> notifier.sendMessage(alertMessage)).start();

        // Insert real timestamp into database
        db.saveAlert(topic, timestamp, msg);
    }
}
