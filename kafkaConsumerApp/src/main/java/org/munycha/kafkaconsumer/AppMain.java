package org.munycha.kafkaconsumer;

import org.munycha.kafkaconsumer.config.ConfigLoader;
import org.munycha.kafkaconsumer.config.TopicConfig;
import org.munycha.kafkaconsumer.consumer.TopicConsumer;
import org.munycha.kafkaconsumer.db.AlertDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {

        ConfigLoader config = new ConfigLoader("config.json");

        // Create database instance ONCE (using DatabaseConfig directly)
        AlertDatabase db = new AlertDatabase(config.getDatabase());

        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        for (TopicConfig t : config.getTopics()) {
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),
                    t.getTopic(),
                    t.getOutput(),
                    config.getTelegramBotToken(),
                    config.getTelegramChatId(),
                    config.getAlertKeywords(),
                    db   // <-- PASS DB INSTANCE HERE
            ));
        }

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow();
        }));
    }
}
