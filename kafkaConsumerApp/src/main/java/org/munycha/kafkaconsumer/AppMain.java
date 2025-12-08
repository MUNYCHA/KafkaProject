package org.munycha.kafkaconsumer;

import org.munycha.kafkaconsumer.config.ConfigLoader;
import org.munycha.kafkaconsumer.config.TopicConfig;
import org.munycha.kafkaconsumer.consumer.TopicConsumer;
import org.munycha.kafkaconsumer.db.AlertDatabase;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {

        // Load application configuration from JSON file
        ConfigLoader config = new ConfigLoader("config/consumer_config.json");

        // Initialize alert database
        AlertDatabase alertDatabase = new AlertDatabase(config.getDatabase());

        // Create a thread pool — one consumer thread per topic
        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        // Start one TopicConsumer per topic defined in config
        for (TopicConfig t : config.getTopics()) {
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),
                    t.getTopic(),
                    Path.of(t.getOutput()),
                    config.getTelegramBotToken(),
                    config.getTelegramChatId(),
                    config.getAlertKeywords(),
                    alertDatabase
            ));
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow();
        }));
    }
}
