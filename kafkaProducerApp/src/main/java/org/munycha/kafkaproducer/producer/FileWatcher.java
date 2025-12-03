package org.munycha.kafkaproducer.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.munycha.kafkaproducer.utility.KafkaTopicValidator;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * FileWatcher is a Runnable task that continuously monitors a specific log file
 * for newly appended lines, and publishes each line as a Kafka message to the
 * configured topic using the provided KafkaProducer.
 */
public class FileWatcher implements Runnable {

    private final Path filePath;
    private final String topic;
    private final KafkaProducer<String, String> producer;
    private final Properties producerProps;

    /**
     * Constructs a FileWatcher that watches a given file and sends its new lines to Kafka.
     *
     * @param filePath       The path to the log file to monitor.
     * @param topic          The Kafka topic to which log lines should be sent.
     * @param producer       The shared Kafka producer instance to use.
     * @param producerProps  The producer configuration properties (used for validation).
     */
    public FileWatcher(Path filePath, String topic, KafkaProducer<String, String> producer, Properties producerProps) {
        this.filePath = filePath;
        this.topic = topic;
        this.producer = producer;
        this.producerProps = producerProps;
    }

    /**
     * Main execution logic for the thread.
     * Continuously monitors the target file and sends new lines to the Kafka topic.
     */
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

        // Start tailing the file from the end (like `tail -f`)
        try (RandomAccessFile reader = new RandomAccessFile(filePath.toFile(), "r")) {
            long filePointer = reader.length();  // Start at the end of the file
            System.out.printf("[%s] Watching file: %s -> Topic: %s%n", java.time.LocalTime.now(), filePath, topic);

            while (!Thread.currentThread().isInterrupted()) {

                long fileLength = filePath.toFile().length();

                // Handle case when file is truncated (e.g., log rotation)
                if (fileLength < filePointer) {
                    filePointer = fileLength;
                    reader.seek(filePointer);
                }

                // If file has grown, read new lines and publish
                else if (fileLength > filePointer) {
                    reader.seek(filePointer);
                    String line;

                    while ((line = reader.readLine()) != null) {
                        // Clean up and sanitize line
                        String msg = line
                                .replace("\uFEFF", "")  // Remove BOM if present
                                .replace("\r", "")
                                .trim();

                        if (msg.isBlank()) continue;  // Skip empty lines

                        // Send to Kafka with callback
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

                    // Update pointer for next read
                    filePointer = reader.getFilePointer();
                }

                // Sleep before next poll
                try {
                    Thread.sleep(500);  // 0.5 sec polling interval
                } catch (InterruptedException ie) {
                    return;  // Clean shutdown if interrupted
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ensure producer is flushed and closed properly
            try {
                producer.flush();
                producer.close();
            } catch (Exception ignored) {}
        }
    }
}
