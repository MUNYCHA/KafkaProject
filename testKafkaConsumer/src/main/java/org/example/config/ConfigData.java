package org.example.config;

import java.util.List;

public class ConfigData {

    private String bootstrapServers;
    private String telegramBotToken;
    private String telegramChatId;
    private List<TopicConfig> topics;

    public ConfigData() {}

    // Getters
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public List<TopicConfig> getTopics() {
        return topics;
    }
}
