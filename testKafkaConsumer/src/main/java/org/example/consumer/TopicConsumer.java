package org.example.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

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

        // Kafka consumer configuration (same as your original)
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "file-log-consumer-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Create consumer and subscribe to exactly one topic
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));

        // Output log file path
        this.outputFile = Paths.get(outputFilePath);
    }

    @Override
    public void run() {

        // Auto-create file and parent directories
        try {
            if (!outputFile.toFile().exists()) {
                outputFile.toFile().getParentFile().mkdirs(); // create parent folders
                outputFile.toFile().createNewFile();          // create file
                System.out.println("Created missing consumer output file: " + outputFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Identical write/flush behavior to your original (append + flush each record)
        try (FileWriter writer = new FileWriter(outputFile.toFile(), true)) {
            System.out.printf("Listening to %s -> writing to %s%n", topic, outputFile);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, String> record : records) {
                    String msg = record.value();

                    // Console log
                    System.out.printf("[%s] (%s) %s%n",
                            java.time.LocalTime.now(), record.topic(), msg);

                    // Append to file and flush immediately (same semantics)
                    writer.write(msg + System.lineSeparator());
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
