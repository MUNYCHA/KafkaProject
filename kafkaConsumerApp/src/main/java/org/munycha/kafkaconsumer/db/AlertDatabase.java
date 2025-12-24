package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class AlertDatabase {
    private final String url;
    private final String user;
    private final String password;
    private final String table;

    public AlertDatabase(DatabaseConfig dbConfig) {
        this.url = dbConfig.getUrl();
        this.user = dbConfig.getUser();
        this.password = dbConfig.getPassword();
        this.table = dbConfig.getTables().getAlertLogTable();
    }

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }

    public void saveAlert(
            String topic,
            long eventTimestamp,
            String logSourceHost,
            String filePath,
            String message
    ) {
        String sql =
                "INSERT INTO " + table +
                        " (topic, event_timestamp, log_source_host, file_path, message) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, topic);
            stmt.setLong(2, eventTimestamp);
            stmt.setString(3, logSourceHost);
            stmt.setString(4, filePath);
            stmt.setString(5, message);

            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("[DB ERROR] Failed to save alert: " + e.getMessage());
        }
    }

}

