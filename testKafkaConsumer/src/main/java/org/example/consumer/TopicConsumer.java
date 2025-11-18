package org.example.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.telegram.TelegramNotifier;

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
    private final TelegramNotifier notifier;

    public TopicConsumer(String bootstrapServers, String topic, String outputFilePath,
                         String botToken, String chatId) {

        this.topic = topic;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "file-log-consumer-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));

        this.outputFile = Paths.get(outputFilePath);

        // Create the Telegram notifier
        this.notifier = new TelegramNotifier(botToken, chatId);
    }

    @Override
    public void run() {
        try (FileWriter writer = new FileWriter(outputFile.toFile(), true)) {

            System.out.printf("Listening to %s -> writing to %s%n", topic, outputFile);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, String> record : records) {
                    String msg = record.value();
                    String lower = msg.toLowerCase();

                    // 1. Print immediately
                    System.out.printf("[%s] (%s) %s%n",
                            java.time.LocalTime.now(), record.topic(), msg);

                    // 2. Save to file
                    writer.write(msg + System.lineSeparator());
                    writer.flush();

                    // 3. Check for alert AFTER printing + saving
                    boolean alert =
                            lower.contains("error") ||
                                    lower.contains("fail") ||
                                    lower.contains("failure") ||
                                    lower.contains("server error") ||
                                    lower.contains("404");

                    if (alert) {
                        // Send alert in background thread so it never blocks the consumer
                        new Thread(() ->
                                notifier.sendMessage("❗ ALERT from " + topic + ":\n" + msg)
                        ).start();
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
