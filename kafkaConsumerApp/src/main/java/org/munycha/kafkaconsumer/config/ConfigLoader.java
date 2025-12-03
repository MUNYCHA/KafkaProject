package org.munycha.kafkaconsumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads and parses configuration from a JSON file in the resources directory.
 *
 * <p>Expected configuration fields include:
 * - Kafka bootstrap servers
 * - Telegram bot token and chat ID
 * - Alert keywords
 * - Per-topic output config
 * - Database config for alert storage
 *
 * <p>This class uses Jackson (`ObjectMapper`) to deserialize JSON into a `ConfigData` POJO.
 */
public class ConfigLoader {

    private final String bootstrapServers;
    private final String telegramBotToken;
    private final String telegramChatId;
    private final List<TopicConfig> topics;
    private final List<String> alertKeywords;
    private final DatabaseConfig database;

    /**
     * Constructs a ConfigLoader by reading and parsing the specified JSON config file.
     *
     * @param filePath The path to the JSON config file, relative to the resources directory.
     * @throws Exception if the file is not found or cannot be parsed.
     */
    public ConfigLoader(String filePath) throws Exception {

        // Load file from classpath (usually in /resources)
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Config file not found in resources: " + filePath);
        }

        // Deserialize JSON to ConfigData POJO
        ObjectMapper mapper = new ObjectMapper();
        ConfigData data = mapper.readValue(inputStream, ConfigData.class);

        // Assign fields from deserialized data
        this.bootstrapServers = data.getBootstrapServers();
        this.telegramBotToken = data.getTelegramBotToken();
        this.telegramChatId = data.getTelegramChatId();
        this.topics = data.getTopics();
        this.alertKeywords = data.getAlertKeywords();
        this.database = data.getDatabase();
    }

    // -----------------------------
    // Getters for external access
    // -----------------------------

    /** @return Kafka bootstrap server addresses */
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /** @return Telegram bot token */
    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    /** @return Telegram chat ID */
    public String getTelegramChatId() {
        return telegramChatId;
    }

    /** @return List of topic configuration entries */
    public List<TopicConfig> getTopics() {
        return topics;
    }

    /** @return List of alert-triggering keywords */
    public List<String> getAlertKeywords() {
        return alertKeywords;
    }

    /** @return Database configuration for storing alerts */
    public DatabaseConfig getDatabase() {
        return database;
    }
}
