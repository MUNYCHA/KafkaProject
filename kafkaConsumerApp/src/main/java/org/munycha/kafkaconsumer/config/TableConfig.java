package org.munycha.kafkaconsumer.config;

public class TableConfig {
    private String alertLogTable;
    private String systemStorageSnapshotTable;
    private String pathStorageTable;

    public TableConfig() {
    }

    public String getAlertLogTable() {
        return alertLogTable;
    }

    public void setAlertLogTable(String alertLogTable) {
        this.alertLogTable = alertLogTable;
    }

    public String getSystemStorageSnapshotTable() {
        return systemStorageSnapshotTable;
    }

    public void setSystemStorageSnapshotTable(String systemStorageSnapshotTable) {
        this.systemStorageSnapshotTable = systemStorageSnapshotTable;
    }

    public String getPathStorageTable() {
        return pathStorageTable;
    }

    public void setPathStorageTable(String pathStorageTable) {
        this.pathStorageTable = pathStorageTable;
    }
}
