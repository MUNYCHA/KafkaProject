package org.example;

import org.example.config.ConfigLoader;
import org.example.config.TopicConfig;
import org.example.consumer.TopicConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {

        ConfigLoader config = new ConfigLoader("config.json");

        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        for (TopicConfig t : config.getTopics()) {
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),
                    t.getTopic(),
                    t.getOutput(),
                    config.getTelegramBotToken(),
                    config.getTelegramChatId(),
                    config.getAlertKeywords()
            ));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow();
        }));
    }
}
