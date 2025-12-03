package org.munycha.kafkaconsumer.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

/**
 * Factory class for creating configured KafkaConsumer instances.
 * <p>
 * This factory encapsulates the Kafka consumer configuration logic,
 * making it easier to reuse and manage consumers for different topics.
 */
public class KafkaConsumerFactory {

    // Holds the properties required to configure a Kafka consumer
    private final Properties consumerProps;

    /**
     * Constructs a KafkaConsumerFactory with topic-specific settings.
     *
     * @param bootstrapServers Comma-separated list of Kafka broker addresses.
     * @param topic            The topic name used to uniquely name the consumer group.
     */
    public KafkaConsumerFactory(String bootstrapServers, String topic) {
        consumerProps = new Properties();

        // Kafka broker address(es)
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Deserializers for key and value — both are plain strings
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Use a unique group ID per topic to isolate consumption
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "file-log-consumer-" + topic);

        // Start consuming from the latest offset if no committed offset is found
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    }

    /**
     * Creates a new KafkaConsumer instance using the current configuration.
     *
     * @return A new {@link KafkaConsumer} configured with the factory's properties.
     */
    public KafkaConsumer<String, String> createConsumer() {
        return new KafkaConsumer<>(consumerProps);
    }

    /**
     * Returns the underlying consumer properties.
     * Useful for validating topics or other advanced configurations.
     *
     * @return Properties object used for consumer creation.
     */
    public Properties getConsumerProps() {
        return consumerProps;
    }
}
