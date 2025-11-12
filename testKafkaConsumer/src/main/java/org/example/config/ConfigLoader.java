package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ConfigLoader {
    private final String bootstrapServers;
    private final List<TopicConfig> topics;

    public ConfigLoader(String filePath) throws Exception {
        // Load file from resources (classpath)
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Config file not found in resources: " + filePath);
        }

        ObjectMapper mapper = new ObjectMapper();
        ConfigData data = mapper.readValue(inputStream, ConfigData.class);

        this.bootstrapServers = data.getBootstrapServers();
        this.topics = data.getTopics();
    }

    public String getBootstrapServers() { return bootstrapServers; }
    public List<TopicConfig> getTopics() { return topics; }
}
