package org.example.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.RandomAccessFile;
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
        File file = filePath.toFile();

        try {
            // ✅ Auto-create directories if missing
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Created directories: " + parentDir.getAbsolutePath());
                }
            }

            // ✅ Auto-create file if missing
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("Created file: " + file.getAbsolutePath());
                }
            }

            // ✅ Use "rw" mode so file can be created/opened safely
            try (RandomAccessFile reader = new RandomAccessFile(file, "rw")) {
                long filePointer = reader.length(); // start from end of file

                while (!Thread.currentThread().isInterrupted()) {
                    long fileLength = file.length();

                    if (fileLength < filePointer) {
                        // File truncated/rotated
                        filePointer = fileLength;
                        reader.seek(filePointer);
                    } else if (fileLength > filePointer) {
                        // New data appended
                        reader.seek(filePointer);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String msg = line.trim(); // remove spaces, tabs, CR/LF

                            // ✅ skip blank or whitespace-only lines
                            if (msg.isEmpty()) continue;

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
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { producer.flush(); } catch (Exception ignored) {}
        }
    }
}
