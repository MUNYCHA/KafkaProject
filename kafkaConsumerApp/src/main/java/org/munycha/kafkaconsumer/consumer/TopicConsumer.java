package org.munycha.kafkaconsumer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.munycha.kafkaconsumer.model.LogEvent;
import org.munycha.kafkaconsumer.telegram.TelegramNotifier;
import org.munycha.kafkaconsumer.db.AlertDatabase;
import org.munycha.kafkaconsumer.utility.KafkaTopicValidator;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final ObjectMapper mapper = new ObjectMapper();

    //Create a single thread executor for Telegram alerts
    private static final ExecutorService alertExecutor = Executors.newSingleThreadExecutor();

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

        LogEvent event = mapper.readValue(record.value(), LogEvent.class);

        String msg = event.getMessage();
        String lowerMsg = msg.toLowerCase();

        String formattedTime =
                Instant.ofEpochMilli(event.getTimestamp())
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.printf(
                "[%s] (%s) %s%n",
                formattedTime,
                event.getTopic(),
                msg
        );

        if (Files.exists(outputFile)) {
            writer.write(
                    formattedTime + " [" + event.getLogSourceHost() + "] " +
                            msg + System.lineSeparator()
            );
            writer.flush();
        }

        boolean alert = alertKeywords.stream()
                .anyMatch(k -> lowerMsg.contains(k));

        if (alert) {
            processAlert(event);
        }
    }

    private void processAlert(LogEvent event) {

        String formattedTime =
                Instant.ofEpochMilli(event.getTimestamp())
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String alertMessage =
                "ALERT\n" +
                        " Time: " + formattedTime + "\n" +
                        " Host: " + event.getLogSourceHost() + "\n" +
                        " File: " + event.getPath() + "\n" +
                        " Topic: " + event.getTopic() + "\n" +
                        " Message: " + event.getMessage();

        alertExecutor.submit(() -> notifier.sendMessage(alertMessage));

        dbExecutor.submit(() ->
                alertDatabase.saveAlert(
                        event.getTopic(),
                        event.getTimestamp(),
                        event.getLogSourceHost(),
                        event.getPath(),
                        event.getMessage()
                )
        );
    }



}