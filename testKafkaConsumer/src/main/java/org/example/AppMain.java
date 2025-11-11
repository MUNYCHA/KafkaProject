package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {
        // Load config.json from the working directory
        ConfigLoader config = new ConfigLoader("config.json");

        // Create a thread pool — one thread for each topic
        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        // Start one TopicConsumer per topic
        for (ConfigLoader.TopicConfig t : config.getTopics()) {
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),
                    t.getTopic(),
                    t.getOutput()
            ));
        }

        // Shutdown hook (Ctrl + C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow(); // interrupts threads; our loop is infinite like your original
        }));
    }
}
