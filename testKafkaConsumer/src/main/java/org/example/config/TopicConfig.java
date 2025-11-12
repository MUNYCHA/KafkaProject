package org.example.config;


public class TopicConfig {
    public String topic;
    public String output;

    // Jackson needs a no-args constructor
    public TopicConfig() {}

    public TopicConfig(String topic, String output) {
        this.topic = topic;
        this.output = output;
    }

    public String getTopic() { return topic; }
    public String getOutput() { return output; }
}

