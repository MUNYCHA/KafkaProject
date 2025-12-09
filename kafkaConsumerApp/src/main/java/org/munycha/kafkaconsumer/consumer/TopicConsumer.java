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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TopicConsumer implements Runnable {

    private final String topic;
    private final Path outputFile;
    private final List<String> alertKeywords;
    private final AlertDatabase alertDatabase;
    private final KafkaConsumer<String, String> consumer;
    private final TelegramNotifier notifier;
    private final KafkaConsumerFactory consumerFactory;

    // Create a thread pool for Telegram notifications
    private static final ExecutorService alertExecutor = Executors.newFixedThreadPool(5);

    // Create a thread pool for database saving
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(3);


    // Shutdown thread pool alertExecutor and dbExecutor
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down executors...");

            alertExecutor.shutdown();
            dbExecutor.shutdown();

            try {
                if (!alertExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    alertExecutor.shutdownNow();
                }
                if (!dbExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    dbExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                alertExecutor.shutdownNow();
                dbExecutor.shutdownNow();
            }

            System.out.println("All executors shut down cleanly.");
        }));
    }


    public TopicConsumer(String bootstrapServers,
                         String topic,
                         Path outputFile,
                         String botToken,
                         String chatId,
                         List<String> alertKeywords,
                         AlertDatabase alertDatabase) {
        this.topic = topic;
        this.outputFile = outputFile;
        this.alertKeywords = alertKeywords;
        this.alertDatabase = alertDatabase;

        this.consumerFactory = new KafkaConsumerFactory(bootstrapServers, topic);
        this.consumer = this.consumerFactory.createConsumer();
        this.consumer.subscribe(Collections.singletonList(this.topic));
        this.notifier = new TelegramNotifier(botToken, chatId);
    }


    private void ensureOutputFileExists() {
        if (!Files.exists(outputFile)) {
            System.err.println(" Output file does NOT exist: " + outputFile);
            System.err.println("   Please create it manually. Consumer will NOT write.");
        }
    }


    private void ensureTopicExists() {
        if (!KafkaTopicValidator.topicExists(topic, this.consumerFactory.getConsumerProps())) {
            System.err.println("[Kafka] Topic does NOT exist: " + topic);
            throw new RuntimeException("Kafka topic does not exist: " + topic);
        }
    }


    @Override
    public void run() {

        ensureTopicExists();
        ensureOutputFileExists();

        try (FileWriter writer = new FileWriter(outputFile.toFile(), true)) {
            System.out.printf("Listening to %s → writing to %s%n", topic, outputFile);

            while (true) {
                ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofMillis(500));

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

    private void handleRecord(ConsumerRecord<String, String> record, FileWriter writer) throws IOException {
        String msg = record.value();
        String lower = msg.toLowerCase();

        System.out.printf(
                "[%s] (%s) Received message → %s%n",
                java.time.LocalTime.now(),
                record.topic(),
                msg
        );


        if (Files.exists(outputFile)) {
            writer.write(msg + System.lineSeparator());
            writer.flush();
        }

        boolean alert = this.alertKeywords.stream().anyMatch(lower::contains);

        if (alert) {
            processAlert(record.topic(), msg);
        }
    }


    private void processAlert(String topic, String msg) {
        LocalDateTime timestamp = LocalDateTime.now();
        String displayTimestamp = timestamp.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String alertMessage =
                "ALERT\n" +
                        " Timestamp: " + displayTimestamp + "\n" +
                        " Topic: " + topic + "\n" +
                        " Message: " + msg;

        // Send message to Telegram asynchronously
        alertExecutor.submit(() -> this.notifier.sendMessage(alertMessage));

        // Save alert to database asynchronously
        dbExecutor.submit(() -> this.alertDatabase.saveAlert(topic, timestamp, msg));

    }
}