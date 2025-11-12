package org.example;

import org.example.config.ConfigLoader; // if ConfigLoader is in a 'config' package
import org.example.config.TopicConfig;  // import the TopicConfig class
import org.example.consumer.TopicConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {
        // Load config.json from the working directory
        ConfigLoader config = new ConfigLoader("config.json");

        // Create a thread pool — one thread per topic
        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        // Start one TopicConsumer per topic
        for (TopicConfig t : config.getTopics()) {  // 🔹 Notice: no more ConfigLoader.TopicConfig
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),
                    t.getTopic(),
                    t.getOutput()
            ));
        }

        // Shutdown hook (Ctrl + C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow();
        }));
    }
}
