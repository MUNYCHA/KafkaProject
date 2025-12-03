package org.munycha.kafkaproducer.utility;

import org.apache.kafka.clients.admin.AdminClient;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaTopicValidator {
    public static boolean topicExists(String topicName, Properties kafkaProps) {
        try (AdminClient admin = AdminClient.create(kafkaProps)) {
            Set<String> existingTopics = admin.listTopics().names().get();
            return existingTopics.contains(topicName);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("[Kafka] Error checking topic: " + e.getMessage());
            return false;
        }
    }
}

