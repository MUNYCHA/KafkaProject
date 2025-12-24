package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;
import org.munycha.kafkaconsumer.model.SystemStorageSnapshot;

import java.sql.*;

public class SystemStorageSnapshotDatabase {

    private final String url;
    private final String user;
    private final String password;
    private final String table;

    public SystemStorageSnapshotDatabase(DatabaseConfig dbConfig) {
        this.url = dbConfig.getUrl();
        this.user = dbConfig.getUser();
        this.password = dbConfig.getPassword();
        this.table = dbConfig.getTables().getSystemStorageSnapshotTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public long saveSnapshot(SystemStorageSnapshot snapshot) throws SQLException {

        String sql =
                "INSERT INTO " + table +
                        " (server_name, server_ip, snapshot_time) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, snapshot.getServerName());
            stmt.setString(2, snapshot.getServerIp());

            // Convert ISO timestamp string to SQL TIMESTAMP
            stmt.setTimestamp(
                    3,
                    Timestamp.from(
                            java.time.Instant.parse(snapshot.getTimestamp())
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
