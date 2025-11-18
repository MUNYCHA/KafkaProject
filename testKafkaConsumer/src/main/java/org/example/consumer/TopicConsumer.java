package org.example.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.telegram.TelegramNotifier;

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
        // ---- Auto-create file + parent folders ----
        try {
            File f = outputFile.toFile();
            File parent = f.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!f.exists()) {
                f.createNewFile();
                System.out.println("Created missing consumer output file: " + outputFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        // ---- Start consuming ----
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

                    // 3. Alert keyword detection
                    boolean alert =
                            lower.contains("error") ||
                                    lower.contains("fail") ||
                                    lower.contains("failure") ||
                                    lower.contains("server error") ||
                                    lower.contains("500") ||
                                    lower.contains("404");


                    // 4. If alert -> send formatted telegram message
                    if (alert) {

                        String timestamp = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        String alertMessage =
                                "❗ ALERT\n" +
                                        "📅 " + timestamp + "\n" +
                                        "📌 Topic: " + topic + "\n" +
                                        "📝 Message:\n" +
                                        msg;

                        new Thread(() -> notifier.sendMessage(alertMessage)).start();
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
