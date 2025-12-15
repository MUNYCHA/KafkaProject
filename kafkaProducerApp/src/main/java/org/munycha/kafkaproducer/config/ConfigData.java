package org.munycha.kafkaproducer.config;

import java.util.List;

public class ConfigData {

    private String bootstrapServers;
    private String logSourceHost;
    private List<FileItem> files;

    public ConfigData() {}

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getLogSourceHost() {
        return logSourceHost;
    }

    public List<FileItem> getFiles() {
        return files;
    }
}
