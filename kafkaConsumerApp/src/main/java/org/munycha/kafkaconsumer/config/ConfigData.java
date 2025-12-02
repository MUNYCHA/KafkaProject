package org.munycha.kafkaconsumer.config;

import java.util.List;

public class ConfigData {

    private String bootstrapServers;
    private String telegramBotToken;
    private String telegramChatId;

    private List<TopicConfig> topics;
    private List<String> alertKeywords;

    private DatabaseConfig database;

    public ConfigData() {}

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

    public List<String> getAlertKeywords() {
        return alertKeywords;
    }

    public DatabaseConfig getDatabase() {   // <-- NEW GETTER
        return database;
    }
}
