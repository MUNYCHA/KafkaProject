package org.munycha.kafkaproducer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ConfigLoader {

    private final String bootstrapServers;
    private final List<FileItem> files;

    public ConfigLoader(String filePath) throws Exception {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Config file not found in resources: " + filePath);
        }

        // Parse the JSON file into a ConfigData object
        ObjectMapper mapper = new ObjectMapper();
        ConfigData data = mapper.readValue(inputStream, ConfigData.class);

        this.bootstrapServers = data.getBootstrapServers();
        this.files = data.getFiles();
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public List<FileItem> getFiles() {
        return files;
    }
}
