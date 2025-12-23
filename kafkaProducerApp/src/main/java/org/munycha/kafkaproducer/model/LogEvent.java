package org.munycha.kafkaproducer.model;

public class LogEvent {

    private String serverName;
    private String path;
    private String topic;
    private long timestamp;
    private String message;

    public LogEvent() {
    }

    public LogEvent(String serverName, String path, String topic, long timestamp, String message) {
        this.serverName = serverName;
        this.path = path;
        this.topic = topic;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
