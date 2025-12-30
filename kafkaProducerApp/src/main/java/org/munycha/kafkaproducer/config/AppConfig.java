package org.munycha.kafkaproducer.config;

import java.util.List;

public class AppConfig {

    private String bootstrapServers;
    private IdentityConfig identity;
    private List<FileConfig> files;
    private StorageMonitoringConfig storageMonitoring;

    public AppConfig() {
    }

    public AppConfig(String bootstrapServers, IdentityConfig identity, List<FileConfig> files, StorageMonitoringConfig storageMonitoring) {
        this.bootstrapServers = bootstrapServers;
        this.identity = identity;
        this.files = files;
        this.storageMonitoring = storageMonitoring;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public IdentityConfig getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityConfig identity) {
        this.identity = identity;
    }

    public List<FileConfig> getFiles() {
        return files;
    }

    public void setFiles(List<FileConfig> files) {
        this.files = files;
    }

    public StorageMonitoringConfig getStorageMonitoring() {
        return storageMonitoring;
    }

    public void setStorageMonitoring(StorageMonitoringConfig storageMonitoring) {
        this.storageMonitoring = storageMonitoring;
    }
}
