package org.example.config;

public class FileItem {
    public String path;
    public String topic;

    public FileItem() {}

    public FileItem(String path, String topic) {
        this.path = path;
        this.topic = topic;
    }

    public String getPath() { return path; }
    public String getTopic() { return topic; }
}
