package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

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
        try (RandomAccessFile reader = new RandomAccessFile(filePath.toFile(), "r")) {
            long filePointer = reader.length(); // start from end of file

            // loop until interrupted (Ctrl+C triggers interrupt via executor.shutdownNow())
            while (!Thread.currentThread().isInterrupted()) {
                long fileLength = filePath.toFile().length();

                if (fileLength < filePointer) {
                    // File truncated/rotated: jump to new end
                    filePointer = fileLength;
                    reader.seek(filePointer);
                } else if (fileLength > filePointer) {
                    // New data appended
                    reader.seek(filePointer);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String msg = line; // note: RandomAccessFile.readLine uses ISO-8859-1

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
                    Thread.sleep(500); // check file every half second
                } catch (InterruptedException ie) {
                    // restore flag and exit loop quietly
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // Log unexpected errors only (normal interrupts won’t land here)
            e.printStackTrace();
        } finally {
            try {
                producer.flush(); // ensure in-flight sends complete before thread exits
            } catch (Exception ignore) { }
        }
    }
}
