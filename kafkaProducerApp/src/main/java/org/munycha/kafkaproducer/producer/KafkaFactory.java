package org.munycha.kafkaproducer.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaFactory {

    private final Properties producerProps;

    public  KafkaFactory(String bootstrapServers) {
        producerProps  = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, "0");
    }

    public KafkaProducer<String ,String> createProducer(){
        return new KafkaProducer<>(producerProps);
    }


    public Properties getProducerProps() {
        return producerProps;
    }
}
