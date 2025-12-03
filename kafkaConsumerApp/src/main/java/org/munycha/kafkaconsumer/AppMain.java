package org.munycha.kafkaconsumer;

import org.munycha.kafkaconsumer.config.ConfigLoader;
import org.munycha.kafkaconsumer.config.TopicConfig;
import org.munycha.kafkaconsumer.consumer.TopicConsumer;
import org.munycha.kafkaconsumer.db.AlertDatabase;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point of the Kafka consumer application.
 * <p>
 * This class:
 * - Loads configuration from `config.json`
 * - Initializes an alert database
 * - Spawns a separate thread per topic to run a `TopicConsumer`
 * - Handles graceful shutdown
 */
public class AppMain {

    public static void main(String[] args) throws Exception {

        // Load application configuration from JSON file
        ConfigLoader config = new ConfigLoader("config.json");

        // Initialize alert database (e.g., SQLite or another backend)
        AlertDatabase alertDatabase = new AlertDatabase(config.getDatabase());

        // Create a thread pool — one consumer thread per topic
        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        // Start one TopicConsumer per topic defined in config
        for (TopicConfig t : config.getTopics()) {
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),         // Kafka broker(s)
                    t.getTopic(),                         // Kafka topic name
                    Path.of(t.getOutput()),               // File to write consumed logs
                    config.getTelegramBotToken(),         // Bot token for Telegram notifications
                    config.getTelegramChatId(),           // Chat ID to send alerts
                    config.getAlertKeywords(),            // Keywords to trigger alerts
                    alertDatabase                         // Shared database for storing alerts
            ));
        }

        // Register shutdown hook to clean up resources gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow(); // Stop all running TopicConsumers
        }));
    }
}
