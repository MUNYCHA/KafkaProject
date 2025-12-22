package org.munycha.kafkaproducer.config;

public class FileConfig {

    private String path;
    private String topic;

    public FileConfig() {}

    public FileConfig(String path, String topic) {
        this.path = path;
        this.topic = topic;
    }

    public String getPath() {
        return path;
    }

    public String getTopic() {
        return topic;
    }
}
