package org.munycha.kafkaproducer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.munycha.kafkaproducer.config.ConfigLoader;
import org.munycha.kafkaproducer.config.FileItem;
import org.munycha.kafkaproducer.producer.FileWatcher;
import org.munycha.kafkaproducer.producer.KafkaFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {
        // Load config.json from the current working directory (parent level)
        ConfigLoader config = new ConfigLoader("config.json");

        // Create ONE shared Kafka producer
        KafkaProducer<String, String> producer = KafkaFactory.create(config.getBootstrapServers());

        // Thread pool — one thread per file
        ExecutorService executor = Executors.newFixedThreadPool(config.getFiles().size());

        // Start a FileWatcher for each configured file
        for (FileItem f : config.getFiles()) {
            String path = f.getPath();
            String topic = f.getTopic();
            Path filePath = Paths.get(path);
            executor.submit(new FileWatcher(filePath, topic, producer));
        }

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down producer...");
            try { producer.flush(); } catch (Exception ignored) {}
            producer.close();
            executor.shutdownNow();
        }));
    }
}
