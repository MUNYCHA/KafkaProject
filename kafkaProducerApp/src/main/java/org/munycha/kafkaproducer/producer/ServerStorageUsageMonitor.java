package org.munycha.kafkaproducer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.munycha.kafkaproducer.config.AppConfig;
import org.munycha.kafkaproducer.model.ServerPathStorageUsage;
import org.munycha.kafkaproducer.model.ServerStorageUsage;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServerStorageUsageMonitor implements Runnable {
    private final KafkaProducer<String, String> producer;
    private final AppConfig config;

    private final ObjectMapper mapper = new ObjectMapper();

    public ServerStorageUsageMonitor(KafkaProducer<String,String> producer, AppConfig config){
        this.producer = producer;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            List<ServerPathStorageUsage> serverPathStorageUsages = ServerPathStorageUsageCollector.collect(this.config.getStorageMonitoring().getPaths());

            ServerStorageUsage serverStorageUsage = new ServerStorageUsage();
            serverStorageUsage.setServerName(config.getIdentity().getServer().getName());
            serverStorageUsage.setServerIp(config.getIdentity().getServer().getIp());

            String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

            serverStorageUsage.setTimestamp(timestamp);
            serverStorageUsage.setServerPathStorageUsages(serverPathStorageUsages);

            String json = mapper.writeValueAsString(serverStorageUsage);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(
                            config.getStorageMonitoring().getTopic(),
                            serverStorageUsage.getServerName(),
                            json
                    );

            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.println(
                            "SYSTEM STORAGE SNAPSHOT SENT | "
                                    + "server=" + serverStorageUsage.getServerName()
                                    + " | topic=" + metadata.topic()
                                    + " | partition=" + metadata.partition()
                                    + " | offset=" + metadata.offset()
                    );
                } else {
                    System.err.println(
                            "FAILED TO SEND SYSTEM STORAGE SNAPSHOT | "
                                    + "server=" + serverStorageUsage.getServerName()
                                    + " | topic=" + record.topic()
                    );
                    exception.printStackTrace();
                }
            });


            producer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

