package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;
import org.munycha.kafkaconsumer.model.ServerStorageUsage;

import java.sql.*;
import java.time.Instant;

public class ServerStorageUsageDB {

    private final String url;
    private final String user;
    private final String password;
    private final String table;

    public ServerStorageUsageDB(DatabaseConfig dbConfig) {
        this.url = dbConfig.getUrl();
        this.user = dbConfig.getUser();
        this.password = dbConfig.getPassword();
        this.table = dbConfig.getTables().getSystemStorageSnapshotTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public long saveSnapshot(ServerStorageUsage serverStorageUsage) throws SQLException {

        String sql =
                "INSERT INTO " + table +
                        " (server_name, server_ip, snapshot_time) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, serverStorageUsage.getServerName());
            stmt.setString(2, serverStorageUsage.getServerIp());

            // Convert ISO timestamp string to SQL TIMESTAMP
            stmt.setTimestamp(
                    3,
                    Timestamp.from(
                            Instant.parse(serverStorageUsage.getTimestamp())
                    )
            );

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        throw new SQLException("Failed to retrieve snapshot ID");
    }
}
