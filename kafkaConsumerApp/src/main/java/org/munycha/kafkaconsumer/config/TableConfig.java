package org.munycha.kafkaconsumer.config;

public class TableConfig {
    private String alertLogTable;
    private String serverStorageUsageTable;
    private String mountPathStorageUsageTable;

    public TableConfig() {
    }

    public String getAlertLogTable() {
        return alertLogTable;
    }

    public void setAlertLogTable(String alertLogTable) {
        this.alertLogTable = alertLogTable;
    }

    public String getServerStorageUsageTable() {
        return serverStorageUsageTable;
    }

    public void setServerStorageUsageTable(String serverStorageUsageTable) {
        this.serverStorageUsageTable = serverStorageUsageTable;
    }

    public String getMountPathStorageUsageTable() {
        return mountPathStorageUsageTable;
    }

    public void setMountPathStorageUsageTable(String mountPathStorageUsageTable) {
        this.mountPathStorageUsageTable = mountPathStorageUsageTable;
    }
}
