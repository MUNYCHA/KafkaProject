package org.munycha.kafkaproducer.config;

public class FileItem {

    private String path;
    private String topic;

    public FileItem() {}

    public FileItem(String path, String topic) {
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
