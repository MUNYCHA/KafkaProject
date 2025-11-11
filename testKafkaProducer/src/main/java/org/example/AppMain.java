package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {
        // Load config.json from current directory
        ConfigLoader config = new ConfigLoader("config.json");

        // Create ONE shared Kafka producer
        KafkaProducer<String, String> producer = KafkaFactory.create(config.getBootstrapServers());

        // Thread pool — one thread per file
        ExecutorService executor = Executors.newFixedThreadPool(config.getFilesNode().size());

        // Start a FileWatcher for each configured file
        for (JsonNode fileNode : config.getFilesNode()) {
            String path = fileNode.get("path").asText();
            String topic = fileNode.get("topic").asText();
            Path filePath = Paths.get(path);

            if (!Files.exists(filePath)) {
                System.err.printf("[%s] File not found: %s%n", java.time.LocalTime.now(), path);
                continue;
            }

            executor.submit(new FileWatcher(filePath, topic, producer));
            System.out.printf("[%s] Watching file: %s -> Topic: %s%n",
                    java.time.LocalTime.now(), path, topic);
        }

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down producer...");
            producer.close();
            executor.shutdownNow();
        }));
    }
}
