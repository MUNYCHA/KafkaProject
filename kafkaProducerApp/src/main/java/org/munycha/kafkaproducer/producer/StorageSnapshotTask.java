package org.munycha.kafkaproducer.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.munycha.kafkaproducer.config.AppConfig;
import org.munycha.kafkaproducer.config.ConfigLoader;
import org.munycha.kafkaproducer.model.PathStorage;
import org.munycha.kafkaproducer.model.SystemStorageSnapshot;
import java.util.List;

public class StorageSnapshotTask implements Runnable {
    private final KafkaProducer<String, String> producer;
    private final AppConfig config;

    private final ObjectMapper mapper = new ObjectMapper();

    public StorageSnapshotTask(KafkaProducer<String,String> producer,AppConfig config){
        this.producer = producer;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            List<PathStorage> pathStorages = StorageCollector.collect(this.config.getSystemResources().getPaths());

            SystemStorageSnapshot snapshot = new SystemStorageSnapshot();
            snapshot.setServerName(config.getIdentity().getServerName());
            snapshot.setServerIp(config.getIdentity().getServerIp());

            String timestamp =
                    java.time.format.DateTimeFormatter.ISO_INSTANT
                            .format(java.time.Instant.ofEpochMilli(System.currentTimeMillis()));

            snapshot.setTimestamp(timestamp);
            snapshot.setPathStorages(pathStorages);

            String json = mapper.writeValueAsString(snapshot);

            producer.send(new ProducerRecord<>(
                    config.getSystemResources().getTopic(),
                    snapshot.getServerName(),
                    json
            ));

            producer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

