package org.munycha.kafkaconsumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;

public class ConfigLoader {

    private final String bootstrapServers;
    private final String telegramBotToken;
    private final String telegramChatId;
    private final List<TopicConfig> topics;
    private final List<String> alertKeywords;
    private final DatabaseConfig database;

    public ConfigLoader(String filePath) throws Exception {

        InputStream inputStream = loadConfigFile(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Config file not found in external path or resources: " + filePath);
        }

        ObjectMapper mapper = new ObjectMapper();
        ConfigData data = mapper.readValue(inputStream, ConfigData.class);

        this.bootstrapServers = data.getBootstrapServers();
        this.telegramBotToken = data.getTelegramBotToken();
        this.telegramChatId = data.getTelegramChatId();
        this.topics = data.getTopics();
        this.alertKeywords = data.getAlertKeywords();
        this.database = data.getDatabase();
    }

    private InputStream loadConfigFile(String filePath) throws FileNotFoundException {

        File externalFile = new File(filePath);

        if (externalFile.exists()) {
            System.out.println("[ConfigLoader] Loading EXTERNAL config: " + externalFile.getAbsolutePath());
            return new FileInputStream(externalFile);
        }

        System.out.println("[ConfigLoader] External config not found. Trying INTERNAL resource: " + filePath);
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }

    public String getBootstrapServers() { return bootstrapServers; }
    public String getTelegramBotToken() { return telegramBotToken; }
    public String getTelegramChatId() { return telegramChatId; }
    public List<TopicConfig> getTopics() { return topics; }
    public List<String> getAlertKeywords() { return alertKeywords; }
    public DatabaseConfig getDatabase() { return database; }
}
