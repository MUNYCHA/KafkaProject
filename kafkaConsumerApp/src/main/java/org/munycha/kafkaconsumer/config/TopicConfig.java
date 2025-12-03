package org.munycha.kafkaconsumer.config;


public class TopicConfig {
    public String topic;
    public String output;

    public TopicConfig() {}

    public TopicConfig(String topic, String output) {
        this.topic = topic;
        this.output = output;
    }

    public String getTopic() { return topic; }
    public String getOutput() { return output; }
}

