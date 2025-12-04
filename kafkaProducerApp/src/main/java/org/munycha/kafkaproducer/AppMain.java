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

        // Load application configuration from JSON
        ConfigLoader config = new ConfigLoader("config.json");

        // Initialize a KafkaProducerFactory with the configured bootstrap servers
        KafkaProducerFactory factory = new KafkaProducerFactory(config.getBootstrapServers());

        // Create a single shared KafkaProducer instance (used by all FileWatcher threads)
        KafkaProducer<String, String> producer = factory.createProducer();

        // Create a thread pool — one worker thread per watched file
        ExecutorService executor = Executors.newFixedThreadPool(config.getFiles().size());

        // Start a FileWatcher for each file-to-topic mapping defined in the configuration
        for (FileItem f : config.getFiles()) {
            String path = f.getPath();
            String topic = f.getTopic();
            Path filePath = Paths.get(path);
            Properties producerProps = factory.getProducerProps();

            // Submit the FileWatcher task to the executor
            executor.submit(new FileWatcher(filePath, topic, producer, producerProps));
        }

        // Register a shutdown hook to close Kafka producer and stop all threads gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down producer...");
            try { producer.flush(); } catch (Exception ignored) {}
            producer.close();
            executor.shutdownNow();
        }));
    }
}
