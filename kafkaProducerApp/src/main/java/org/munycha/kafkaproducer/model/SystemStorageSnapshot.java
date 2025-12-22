package org.munycha.kafkaproducer.model;

import java.time.Instant;
import java.util.List;

public class SystemStorageSnapshot {
    private String serverName;
    private String serverIp;
    private Instant timestamp;
    private List<PathStorage> pathStorages;

    public SystemStorageSnapshot() {
    }

    public SystemStorageSnapshot(String serverName, String serverIp, Instant timestamp, List<PathStorage> pathStorages) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.timestamp = timestamp;
        this.pathStorages = pathStorages;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<PathStorage> getPathStorages() {
        return pathStorages;
    }

    public void setPathStorages(List<PathStorage> pathStorages) {
        this.pathStorages = pathStorages;
    }
}

