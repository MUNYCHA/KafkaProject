package org.munycha.kafkaproducer.config;

public class IdentityConfig {
    private String serverName;
    private String serverIp;

    public IdentityConfig() {
    }

    public IdentityConfig(String serverName, String serverIp) {
        this.serverName = serverName;
        this.serverIp = serverIp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
