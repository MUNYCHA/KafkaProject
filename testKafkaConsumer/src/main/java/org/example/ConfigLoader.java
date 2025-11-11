package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {
    private final String bootstrapServers;
    private final List<TopicConfig> topics;

    public ConfigLoader(String filePath) throws Exception {
        String configContent = new String(Files.readAllBytes(Paths.get(filePath)));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(configContent);

        this.bootstrapServers = root.get("bootstrapServers").asText();

        this.topics = new ArrayList<>();
        JsonNode topicsNode = root.get("topics");
        for (JsonNode t : topicsNode) {
            String topic = t.get("topic").asText();
            String output = t.get("output").asText();
            topics.add(new TopicConfig(topic, output));
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public List<TopicConfig> getTopics() {
        return topics;
    }

    // Simple value object for one topic line in config.json
    public static class TopicConfig {
        private final String topic;
        private final String output;

        public TopicConfig(String topic, String output) {
            this.topic = topic;
            this.output = output;
        }

        public String getTopic() {
            return topic;
        }

        public String getOutput() {
            return output;
        }
    }
}
