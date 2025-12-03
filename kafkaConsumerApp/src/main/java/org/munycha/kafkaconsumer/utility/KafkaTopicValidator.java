package org.munycha.kafkaconsumer.utility;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Utility class to validate the existence of Kafka topics using the Kafka AdminClient.
 */
public class KafkaTopicValidator {

    /**
     * Checks whether a given Kafka topic exists by attempting to describe it.
     *
     * @param topicName   The name of the topic to check.
     * @param kafkaProps  Properties required to connect to the Kafka cluster (typically includes bootstrap servers).
     * @return true if the topic exists; false otherwise.
     */
    public static boolean topicExists(String topicName, Properties kafkaProps) {
        // Use try-with-resources to ensure AdminClient is closed properly
        try (AdminClient admin = AdminClient.create(kafkaProps)) {

            // Describe the topic to check if it exists
            DescribeTopicsResult describeResult = admin.describeTopics(Collections.singletonList(topicName));
            KafkaFuture<Map<String, org.apache.kafka.clients.admin.TopicDescription>> future = describeResult.all();

            // If the topic does not exist, this will throw an exception
            future.get();
            return true;

        } catch (ExecutionException e) {
            // Specifically handle unknown topic error
            if (e.getCause() instanceof org.apache.kafka.common.errors.UnknownTopicOrPartitionException) {
                System.err.println("[Kafka] Topic not found: " + topicName);
            } else {
                // Handle all other execution exceptions
                System.err.println("[Kafka] Error checking topic: " + e.getMessage());
            }
            return false;

        } catch (Exception e) {
            // Catch any unexpected exceptions during the check
            System.err.println("[Kafka] Unexpected error: " + e.getMessage());
            return false;
        }
    }
}
