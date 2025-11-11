package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileLogProducer {

    // Watches a single log file for new lines and sends them to Kafka
    static class FileWatcher implements Runnable {
        private final Path filePath;
        private final String topic;
        private final KafkaProducer<String, String> producer;

        FileWatcher(Path filePath, String topic, KafkaProducer<String, String> producer) {
            this.filePath = filePath;
            this.topic = topic;
            this.producer = producer;
        }

        @Override
        public void run() {
            try (RandomAccessFile reader = new RandomAccessFile(filePath.toFile(), "r")) {
                long filePointer = reader.length(); // start reading from end of file

                while (true) {
                    long fileLength = filePath.toFile().length();

                    // Handle file truncation or rotation
                    if (fileLength < filePointer) {
                        filePointer = fileLength;
                    }
                    // New data added to the file
                    else if (fileLength > filePointer) {
                        reader.seek(filePointer);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String msg = line;

                            ProducerRecord<String, String> record = new ProducerRecord<>(topic, msg);
                            producer.send(record, (metadata, ex) -> {
                                if (ex != null) {
                                    System.err.printf("[%s] Topic: %-15s Error: %s%n",
                                            java.time.LocalTime.now(), topic, ex.getMessage());
                                } else {
                                    System.out.printf("[%s] Topic: %-15s Sent message: %s%n",
                                            java.time.LocalTime.now(), topic, msg);
                                }
                            });
                        }
                        filePointer = reader.getFilePointer();
                    }

                    Thread.sleep(500); // poll file every half second
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Load config.json (must be in same directory as the JAR)
        String configContent = new String(Files.readAllBytes(Paths.get("config.json")));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(configContent);

        String bootstrapServers = root.get("bootstrapServers").asText();
        JsonNode filesNode = root.get("files");

        // Kafka producer configuration
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "0");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Create a thread for each file watcher
        ExecutorService executor = Executors.newFixedThreadPool(filesNode.size());

        for (JsonNode fileNode : filesNode) {
            String path = fileNode.get("path").asText();
            String topic = fileNode.get("topic").asText();
            Path filePath = Paths.get(path);

            if (!Files.exists(filePath)) {
                System.err.printf("[%s] File not found: %s%n", java.time.LocalTime.now(), path);
                continue;
            }

            executor.submit(new FileWatcher(filePath, topic, producer));
            System.out.printf("[%s] Watching file: %s -> Topic: %s%n", java.time.LocalTime.now(), path, topic);
        }

        // Graceful shutdown when the program is stopped
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down producer...");
            producer.close();
            executor.shutdownNow();
        }));
    }
}
