package org.munycha.kafkaproducer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.munycha.kafkaproducer.config.ConfigLoader;
import org.munycha.kafkaproducer.config.FileItem;
import org.munycha.kafkaproducer.producer.FileWatcher;
import org.munycha.kafkaproducer.producer.KafkaProducerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point for the Kafka producer application.
 *
 * This application reads configuration from a JSON file, initializes a single shared KafkaProducer,
 * and starts a FileWatcher thread for each configured file-to-topic mapping.
 */
public class AppMain {

    public static void main(String[] args) throws Exception {

        // ---------------------------------------------------------
        // 1. Load application configuration from config.json
        // ---------------------------------------------------------
        ConfigLoader config = new ConfigLoader("config.json");

        // ---------------------------------------------------------
        // 2. Initialize a KafkaProducerFactory using the bootstrap servers
        // ---------------------------------------------------------
        KafkaProducerFactory factory = new KafkaProducerFactory(config.getBootstrapServers());

        // ---------------------------------------------------------
        // 3. Create a single shared KafkaProducer instance
        //    (reused across all FileWatcher threads)
        // ---------------------------------------------------------
        KafkaProducer<String, String> producer = factory.createProducer();

        // ---------------------------------------------------------
        // 4. Set up a thread pool (1 thread per file being watched)
        // ---------------------------------------------------------
        ExecutorService executor = Executors.newFixedThreadPool(config.getFiles().size());

        // ---------------------------------------------------------
        // 5. Start a FileWatcher thread for each file-to-topic mapping
        // ---------------------------------------------------------
        for (FileItem f : config.getFiles()) {
            String path = f.getPath();                      // File path to watch
            String topic = f.getTopic();                    // Kafka topic to produce to
            Path filePath = Paths.get(path);                // Convert to Path object
            Properties producerProps = factory.getProducerProps();  // For topic validation

            // Submit a new FileWatcher task to the executor
            executor.submit(new FileWatcher(filePath, topic, producer, producerProps));
        }

        // ---------------------------------------------------------
        // 6. Register a shutdown hook to gracefully close resources
        // ---------------------------------------------------------
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down producer...");
            try { producer.flush(); } catch (Exception ignored) {}
            producer.close();              // Close Kafka producer
            executor.shutdownNow();        // Stop all threads
        }));
    }
}
