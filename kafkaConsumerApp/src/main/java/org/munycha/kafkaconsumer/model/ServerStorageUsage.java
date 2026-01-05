package org.munycha.kafkaconsumer.model;

import java.util.List;

public class ServerStorageUsage {
    private String systemId;
    private String systemName;
    private String serverName;
    private String serverIp;
    private String timestamp;
    private List<ServerPathStorageUsage> serverPathStorageUsages;

    public ServerStorageUsage() {
    }

    public ServerStorageUsage(String systemId, String systemName, String serverName, String serverIp, String timestamp, List<ServerPathStorageUsage> serverPathStorageUsages) {
        this.systemId = systemId;
        this.systemName = systemName;
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.timestamp = timestamp;
        this.serverPathStorageUsages = serverPathStorageUsages;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<ServerPathStorageUsage> getServerPathStorageUsages() {
        return serverPathStorageUsages;
    }

    public void setServerPathStorageUsages(List<ServerPathStorageUsage> serverPathStorageUsages) {
        this.serverPathStorageUsages = serverPathStorageUsages;
    }
}

