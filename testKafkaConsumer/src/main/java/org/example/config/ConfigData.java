package org.example.config;

import java.util.List;

public class ConfigData {
    public String bootstrapServers;
    public List<TopicConfig> topics;

    public ConfigData() {}

    public String getBootstrapServers() { return bootstrapServers; }
    public List<TopicConfig> getTopics() { return topics; }
}

