package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;
import org.munycha.kafkaconsumer.model.ServerPathStorageUsage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServerPathStorageUsageDB {

    private final String url;
    private final String user;
    private final String password;
    private final String table;

    public ServerPathStorageUsageDB(DatabaseConfig dbConfig) {
        this.url = dbConfig.getUrl();
        this.user = dbConfig.getUser();
        this.password = dbConfig.getPassword();
        this.table = dbConfig.getTables().getPathStorageTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void savePath(long serverStorageUsageId, ServerPathStorageUsage serverPathStorageUsage) throws SQLException {

        String sql =
                "INSERT INTO " + table +
                        " (server_storage_usage_id, path, total_bytes, used_bytes, used_percent) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, serverStorageUsageId);
            stmt.setString(2, serverPathStorageUsage.getPath());
            stmt.setLong(3, serverPathStorageUsage.getTotalBytes());
            stmt.setLong(4, serverPathStorageUsage.getUsedBytes());
            stmt.setDouble(5, serverPathStorageUsage.getUsedPercent());

            stmt.executeUpdate();
        }
    }
}
