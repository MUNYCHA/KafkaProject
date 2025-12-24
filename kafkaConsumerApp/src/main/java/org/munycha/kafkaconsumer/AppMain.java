package org.munycha.kafkaconsumer;

import org.munycha.kafkaconsumer.config.AppConfig;
import org.munycha.kafkaconsumer.config.ConfigLoader;
import org.munycha.kafkaconsumer.config.TopicConfig;
import org.munycha.kafkaconsumer.consumer.TopicConsumer;
import org.munycha.kafkaconsumer.db.AlertDatabase;
import org.munycha.kafkaconsumer.db.PathStorageDatabase;
import org.munycha.kafkaconsumer.db.SystemStorageSnapshotDatabase;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppMain {

    public static void main(String[] args) throws Exception {

        // Load application configuration from JSON file
        ConfigLoader loader = new ConfigLoader("config/consumer_config.json");
        AppConfig config = loader.load();

        // Initialize alert database
        AlertDatabase alertDatabase = new AlertDatabase(config.getDatabase());

        //Initialize system storage snapshot database
        SystemStorageSnapshotDatabase systemStorageSnapshotDatabase = new SystemStorageSnapshotDatabase(config.getDatabase());

        //Initialize path storage database
        PathStorageDatabase pathStorageDatabase = new PathStorageDatabase(config.getDatabase());

        // Create a thread pool — one consumer thread per topic
        ExecutorService executor = Executors.newFixedThreadPool(config.getTopics().size());

        // Start one TopicConsumer per topic defined in config
        for (TopicConfig t : config.getTopics()) {
            executor.submit(new TopicConsumer(
                    config.getBootstrapServers(),
                    t.getTopic(),
                    t.getType(),
                    Path.of(t.getOutput()),
                    config.getTelegramBotToken(),
                    config.getTelegramChatId(),
                    config.getAlertKeywords(),
                    alertDatabase,
                    systemStorageSnapshotDatabase,
                    pathStorageDatabase
            ));
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down consumers...");
            executor.shutdownNow();
        }));
    }
}
