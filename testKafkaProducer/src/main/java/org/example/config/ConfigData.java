package org.example.config;

import java.util.List;


public class ConfigData {
    public String bootstrapServers;
    public List<FileItem> files;

    public ConfigData() {}

    public String getBootstrapServers() { return bootstrapServers; }
    public List<FileItem> getFiles() { return files; }
}
