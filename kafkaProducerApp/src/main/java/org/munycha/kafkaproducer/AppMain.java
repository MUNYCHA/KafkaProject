package org.munycha.kafkaproducer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.munycha.kafkaproducer.config.AppConfig;
import org.munycha.kafkaproducer.config.ConfigLoader;
import org.munycha.kafkaproducer.config.FileConfig;
import org.munycha.kafkaproducer.producer.FileWatcher;
import org.munycha.kafkaproducer.producer.KafkaProducerFactory;
import org.munycha.kafkaproducer.producer.ServerStorageUsageMonitor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppMain {

    public static void main(String[] args) throws Exception {

        // Load config ONCE
        ConfigLoader loader = new ConfigLoader("config/producer_config.json");
        AppConfig config = loader.load();

        // Kafka producer
        KafkaProducerFactory factory =
                new KafkaProducerFactory(config.getBootstrapServers());
        KafkaProducer<String, String> producer = factory.createProducer();

        // File watcher threads
        ExecutorService executor =
                Executors.newFixedThreadPool(config.getFiles().size());


        for (FileConfig f : config.getFiles()) {
            Path filePath = Paths.get(f.getPath());
            Properties producerProps = factory.getProducerProps();

            executor.submit(
                    new FileWatcher(
                            filePath,
                            f.getTopic(),
                            config.getIdentity().getServer().getName(),
                            producer,
                            producerProps
                    )
            );
        }

        // Storage snapshot scheduler
        ScheduledExecutorService storageScheduler = null;

        if (config.getStorageMonitoring() != null &&
                config.getStorageMonitoring().isEnabled()) {

            storageScheduler = Executors.newSingleThreadScheduledExecutor();

            storageScheduler.scheduleAtFixedRate(
                    new ServerStorageUsageMonitor(producer, config),
                    0,
                    config.getStorageMonitoring().getIntervalHours(),
                    TimeUnit.HOURS
            );
        }

        ScheduledExecutorService finalStorageScheduler = storageScheduler;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down producer...");

            if (finalStorageScheduler != null) {
                finalStorageScheduler.shutdownNow();
            }

            try { producer.flush(); } catch (Exception ignored) {}
            producer.close();
            executor.shutdownNow();
        }));
    }
}
