package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileLogConsumer {

    // This class handles consuming messages from one Kafka topic
    // and writing them into a specific log file
    static class TopicConsumer implements Runnable {
        private final String topic;
        private final KafkaConsumer<String, String> consumer;
        private final Path outputFile;

        // Constructor: creates a KafkaConsumer for the given topic
        TopicConsumer(String bootstrapServers, String topic, String outputFilePath) {
            this.topic = topic;

            // Kafka consumer configuration properties
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Kafka broker address
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()); // key deserializer
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()); // value deserializer
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "file-log-consumer-" + topic); // unique consumer group per topic
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // consume only new messages

            // Create and subscribe the Kafka consumer to a single topic
            this.consumer = new KafkaConsumer<>(props);
            this.consumer.subscribe(Collections.singletonList(topic));

            // File path where messages will be written
            this.outputFile = Paths.get(outputFilePath);
        }

        @Override
        public void run() {
            // This method runs in a separate thread for each topic
            try (FileWriter writer = new FileWriter(outputFile.toFile(), true)) {
                System.out.printf("Listening to %s -> writing to %s%n", topic, outputFile);

                // Continuously poll Kafka for new messages
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                    // Loop through all received records
                    for (ConsumerRecord<String, String> record : records) {
                        String msg = record.value();

                        // Print the message to the console
                        System.out.printf("[%s] (%s) %s%n", java.time.LocalTime.now(), record.topic(), msg);


                        // Write the message to the output log file
                        writer.write(msg + System.lineSeparator());
                        writer.flush(); // ensure it’s written immediately
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Close the Kafka consumer when done
                consumer.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Read the configuration file (config.json)
        String configContent = new String(Files.readAllBytes(Paths.get("config.json")));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(configContent);

        // Get Kafka bootstrap servers and topic list from JSON
        String bootstrapServers = root.get("bootstrapServers").asText();
        JsonNode topicsNode = root.get("topics");

        // Create a thread pool — one thread for each topic
        ExecutorService executor = Executors.newFixedThreadPool(topicsNode.size());

        // Create and start a TopicConsumer for each topic in the config file
        for (JsonNode t : topicsNode) {
            String topic = t.get("topic").asText();
            String outputFile = t.get("output").asText();
            executor.submit(new TopicConsumer(bootstrapServers, topic, outputFile));
        }

        // Add a shutdown hook so the program closes cleanly when stopped (Ctrl + C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow();
        }));
    }
}
