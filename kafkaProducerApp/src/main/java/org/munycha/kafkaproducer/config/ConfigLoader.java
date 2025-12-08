package org.munycha.kafkaproducer.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;

public class ConfigLoader {

    private final String bootstrapServers;
    private final List<FileItem> files;

    public ConfigLoader(String filePath) throws Exception {

        InputStream inputStream = loadConfigFile(filePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Config file not found (external or internal): " + filePath);
        }

        ObjectMapper mapper = new ObjectMapper();
        ConfigData data = mapper.readValue(inputStream, ConfigData.class);

        this.bootstrapServers = data.getBootstrapServers();
        this.files = data.getFiles();
    }


    private InputStream loadConfigFile(String filePath) throws FileNotFoundException {

        File externalFile = new File(filePath);

        if (externalFile.exists()) {
            System.out.println("[ConfigLoader] Loading EXTERNAL config: " + externalFile.getAbsolutePath());
            return new FileInputStream(externalFile);
        }

        System.out.println("[ConfigLoader] External config not found. Loading INTERNAL config: " + filePath);
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public List<FileItem> getFiles() {
        return files;
    }
}
