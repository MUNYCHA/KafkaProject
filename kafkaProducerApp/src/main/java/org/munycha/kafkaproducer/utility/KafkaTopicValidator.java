package org.munycha.kafkaproducer.utility;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Utility class for validating the existence of Kafka topics.
 * <p>
 * This class uses Kafka's AdminClient API to check whether a given topic
 * exists on the configured Kafka cluster.
 */
public class KafkaTopicValidator {

    /**
     * Checks if a given topic exists on the Kafka cluster using the provided configuration.
     *
     * @param topicName  The name of the topic to check.
     * @param kafkaProps Kafka client properties (e.g., bootstrap servers).
     * @return true if the topic exists, false otherwise.
     */
    public static boolean topicExists(String topicName, Properties kafkaProps) {
        // Use try-with-resources to auto-close AdminClient
        try (AdminClient admin = AdminClient.create(kafkaProps)) {

            // Use describeTopics API which throws if the topic does not exist
            DescribeTopicsResult describeResult = admin.describeTopics(Collections.singletonList(topicName));
            KafkaFuture<Map<String, org.apache.kafka.clients.admin.TopicDescription>> future = describeResult.all();

            // This call will throw ExecutionException if the topic does not exist
            future.get();
            return true;

        } catch (ExecutionException e) {
            // Handle case when topic does not exist
            if (e.getCause() instanceof org.apache.kafka.common.errors.UnknownTopicOrPartitionException) {
                System.err.println("[Kafka] Topic not found: " + topicName);
            } else {
                // Handle other types of execution exceptions
                System.err.println("[Kafka] Error checking topic: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            // Catch other unexpected exceptions (e.g., InterruptedException)
            System.err.println("[Kafka] Unexpected error: " + e.getMessage());
            return false;
        }
    }
}
