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

/**
 * TopicConsumer is a Runnable task that continuously consumes messages
 * from a specific Kafka topic and writes them to a log file.
 *
 * It also detects alert messages based on user-defined keywords
 * and sends those alerts to Telegram and stores them in a database.
 */
public class TopicConsumer implements Runnable {

    // Kafka topic to consume from
    private final String topic;

    // Output log file path for storing consumed messages
    private final Path outputFile;

    // List of keywords that trigger alerts
    private final List<String> alertKeywords;

    // Database instance used for persisting alert logs
    private final AlertDatabase alertDatabase;

    // Kafka consumer instance
    private final KafkaConsumer<String, String> consumer;

    // Telegram alert notifier
    private final TelegramNotifier notifier;

    // Consumer factory used to create the Kafka consumer
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



    /**
     * Constructs a TopicConsumer with all required configuration.
     *
     * @param bootstrapServers Kafka server addresses
     * @param topic            Kafka topic name to consume from
     * @param outputFile       Path to the file where consumed messages will be written
     * @param botToken         Telegram bot token
     * @param chatId           Telegram chat ID for sending alerts
     * @param alertKeywords    List of keywords to detect in messages
     * @param alertDatabase    Database used to store alert entries
     */
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

    /**
     * Verifies that the configured output file exists.
     * If it doesn't, a warning is printed and the consumer continues.
     */
    private void ensureOutputFileExists() {
        if (!Files.exists(outputFile)) {
            System.err.println(" Output file does NOT exist: " + outputFile);
            System.err.println("   Please create it manually. Consumer will NOT write.");
        }
    }

    /**
     * Verifies that the specified Kafka topic exists.
     * Throws a runtime exception if the topic is not found.
     */
    private void ensureTopicExists() {
        if (!KafkaTopicValidator.topicExists(topic, this.consumerFactory.getConsumerProps())) {
            System.err.println("[Kafka] Topic does NOT exist: " + topic);
            throw new RuntimeException("Kafka topic does not exist: " + topic);
        }
    }

    /**
     * Starts the consumer loop. Continuously polls Kafka for new messages,
     * writes them to the output file, and checks for alert keywords.
     */
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

    /**
     * Handles a single Kafka record:
     * - Writes it to the output file
     * - Triggers alert processing if keywords are matched
     *
     * @param record the Kafka record
     * @param writer the file writer
     * @throws IOException if file writing fails
     */
    private void handleRecord(ConsumerRecord<String, String> record, FileWriter writer) throws IOException {
        String msg = record.value();
        String lower = msg.toLowerCase();

        System.out.printf("[%s] (%s) %s%n",
                java.time.LocalTime.now(), record.topic(), msg);

        if (Files.exists(outputFile)) {
            writer.write(msg + System.lineSeparator());
            writer.flush();
        }

        boolean alert = this.alertKeywords.stream().anyMatch(lower::contains);

        if (alert) {
            processAlert(record.topic(), msg);
        }
    }

    /**
     * Processes an alert by sending it to Telegram and storing it in the database.
     * This is run in a separate thread to avoid blocking the main consumer loop.
     *
     * @param topic the Kafka topic name
     * @param msg   the message that triggered the alert
     */
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
