package org.munycha.kafkaproducer.model;

public class LogEvent {

    private String logSourceHost;
    private String path;
    private String topic;
    private long timestamp;
    private String message;

    public LogEvent() {
    }

    public LogEvent(
            String logSourceHost,
            String path,
            String topic,
            long timestamp,
            String message
    ) {
        this.logSourceHost = logSourceHost;
        this.path = path;
        this.topic = topic;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getLogSourceHost() {
        return logSourceHost;
    }

    public String getPath() {
        return path;
    }

    public String getTopic() {
        return topic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setLogSourceHost(String logSourceHost) {
        this.logSourceHost = logSourceHost;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
