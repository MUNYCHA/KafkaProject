package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;
import org.munycha.kafkaconsumer.model.PathStorage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PathStorageDatabase {

    private final String url;
    private final String user;
    private final String password;
    private final String table;

    public PathStorageDatabase(DatabaseConfig dbConfig) {
        this.url = dbConfig.getUrl();
        this.user = dbConfig.getUser();
        this.password = dbConfig.getPassword();
        this.table = dbConfig.getTables().getPathStorageTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void savePath(long snapshotId, PathStorage ps) throws SQLException {

        String sql =
                "INSERT INTO " + table +
                        " (snapshot_id, path, total_bytes, used_bytes, used_percent) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, snapshotId);
            stmt.setString(2, ps.getPath());
            stmt.setLong(3, ps.getTotalBytes());
            stmt.setLong(4, ps.getUsedBytes());
            stmt.setDouble(5, ps.getUsedPercent());

            stmt.executeUpdate();
        }
    }
}
