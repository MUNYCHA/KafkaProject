package org.example.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class TopicConsumer implements Runnable {
    private final String topic;
    private final KafkaConsumer<String, String> consumer;
    private final Path outputFile;

    public TopicConsumer(String bootstrapServers, String topic, String outputFilePath) {
        this.topic = topic;

        // Kafka consumer configuration
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "file-log-consumer-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));

        // Output log file path
        this.outputFile = Paths.get(outputFilePath);
    }

    @Override
    public void run() {
        File file = outputFile.toFile();

        try {
            // ✅ Auto-create directories if missing
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Created directories: " + parentDir.getAbsolutePath());
                }
            }

            // ✅ Auto-create the output file if missing
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("Created output file: " + file.getAbsolutePath());
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                System.out.printf("Listening to %s -> writing to %s%n", topic, outputFile);

                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                    for (ConsumerRecord<String, String> record : records) {
                        String msg = record.value();

                        // Console log
                        System.out.printf("[%s] (%s) %s%n",
                                java.time.LocalTime.now(), record.topic(), msg);

                        // Append to file and flush immediately
                        writer.write(msg + System.lineSeparator());
                        writer.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
