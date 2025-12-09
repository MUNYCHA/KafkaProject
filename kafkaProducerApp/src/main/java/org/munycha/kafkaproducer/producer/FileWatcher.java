package org.munycha.kafkaproducer.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.munycha.kafkaproducer.utility.KafkaTopicValidator;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileWatcher implements Runnable {

    private final Path filePath;
    private final String topic;
    private final KafkaProducer<String, String> producer;
    private final Properties producerProps;

    public FileWatcher(Path filePath, String topic, KafkaProducer<String, String> producer, Properties producerProps) {
        this.filePath = filePath;
        this.topic = topic;
        this.producer = producer;
        this.producerProps = producerProps;
    }

    @Override
    public void run() {

        // Validate that the topic exists before starting
        if (!KafkaTopicValidator.topicExists(topic, producerProps)) {
            System.err.println("[Kafka] Topic " + topic + " does NOT exist. Skipping watcher for this file.");
            return;
        }

        // Validate that the target file exists
        if (!Files.exists(filePath)) {
            System.err.println("Producer output file does NOT exist: " + filePath);
            System.err.println("Please create it manually. Producer will NOT write.");
            return;
        }

        try (RandomAccessFile reader = new RandomAccessFile(filePath.toFile(), "r")) {
            long filePointer = reader.length();
            System.out.printf("[%s] Watching file: %s -> Topic: %s%n", java.time.LocalTime.now(), filePath, topic);

            while (!Thread.currentThread().isInterrupted()) {

                long fileLength = filePath.toFile().length();

                if (fileLength < filePointer) {
                    filePointer = fileLength;
                    reader.seek(filePointer);
                }
                else if (fileLength > filePointer) {
                    reader.seek(filePointer);
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String msg = line
                                .replace("\uFEFF", "")  // Remove BOM if present
                                .replace("\r", "")
                                .trim();

                        if (msg.isBlank()) continue;

                        producer.send(new ProducerRecord<>(topic, msg), (metadata, ex) -> {
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
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                producer.flush();
                producer.close();
            } catch (Exception ignored) {}
        }
    }
}