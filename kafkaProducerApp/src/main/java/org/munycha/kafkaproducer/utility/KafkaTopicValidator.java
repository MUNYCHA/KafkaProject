package org.munycha.kafkaproducer.utility;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class KafkaTopicValidator {

    public static boolean topicExists(String topicName, Properties kafkaProps) {

        try (AdminClient admin = AdminClient.create(kafkaProps)) {

            DescribeTopicsResult describeResult = admin.describeTopics(Collections.singletonList(topicName));
            KafkaFuture<Map<String, org.apache.kafka.clients.admin.TopicDescription>> future = describeResult.all();

            future.get();
            return true;

        } catch (ExecutionException e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.UnknownTopicOrPartitionException) {
                System.err.println("[Kafka] Topic not found: " + topicName);
            } else {
                System.err.println("[Kafka] Error checking topic: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            System.err.println("[Kafka] Unexpected error: " + e.getMessage());
            return false;
        }
    }
}
