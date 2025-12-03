package org.munycha.kafkaproducer.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Factory class responsible for creating KafkaProducer instances.
 * <p>
 * This encapsulates all Kafka producer configuration in one place,
 * keeping producer setup clean and maintainable. The same producer
 * configuration can be reused across multiple FileWatcher threads.
 */
public class KafkaProducerFactory {

    /** Shared Kafka producer properties used to create producer instances. */
    private final Properties producerProps;

    /**
     * Constructs a KafkaProducerFactory with all necessary configuration
     * for connecting to a Kafka cluster and serializing messages.
     *
     * @param bootstrapServers Kafka broker connection string (e.g., "localhost:9092")
     */
    public KafkaProducerFactory(String bootstrapServers) {

        // Initialize producer configuration
        producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serialize keys and values as Strings
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Require leader acknowledgment only (fast but not fully durable)
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");

        // Send immediately with no artificial delay
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, "0");
    }

    /**
     * Creates and returns a new KafkaProducer instance using the configured properties.
     *
     * @return a configured KafkaProducer ready for sending messages
     */
    public KafkaProducer<String, String> createProducer() {
        return new KafkaProducer<>(producerProps);
    }

    /**
     * Returns the underlying producer configuration.
     * Useful for passing into utilities such as topic validators.
     *
     * @return the Kafka producer Properties object
     */
    public Properties getProducerProps() {
        return producerProps;
    }
}
