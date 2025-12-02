package org.munycha.kafkaproducer.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWatcher implements Runnable {
    private final Path filePath;
    private final String topic;
    private final KafkaProducer<String, String> producer;

    public FileWatcher(Path filePath, String topic, KafkaProducer<String, String> producer) {
        this.filePath = filePath;
        this.topic = topic;
        this.producer = producer;
    }

    @Override
    public void run() {

        if (!Files.exists(filePath)) {
            System.err.println("Producer output file does NOT exist: " + filePath);
            System.err.println("Please create it manually. Producer will NOT write.");
            return;
        }


        // ---- Begin file watching ----
        try (RandomAccessFile reader = new RandomAccessFile(filePath.toFile(), "r")) {
            long filePointer = reader.length(); // start from end of file
            System.out.printf("[%s] Watching file: %s -> Topic: %s%n", java.time.LocalTime.now(), filePath, topic);
            while (!Thread.currentThread().isInterrupted()) {

                long fileLength = filePath.toFile().length();

                if (fileLength < filePointer) {
                    filePointer = fileLength;
                    reader.seek(filePointer);

                } else if (fileLength > filePointer) {
                    reader.seek(filePointer);
                    String line;

                    while ((line = reader.readLine()) != null) {

                        String msg = line
                                .replace("\uFEFF", "")
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
                    return; // clean shutdown
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { producer.flush(); } catch (Exception ignored) {}
        }
    }

}
