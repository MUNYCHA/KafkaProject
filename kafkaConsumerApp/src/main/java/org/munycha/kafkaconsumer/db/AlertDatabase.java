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

    public AlertDatabase(DatabaseConfig cfg) {
        this.url = cfg.getUrl();
        this.user = cfg.getUser();
        this.password = cfg.getPassword();
        this.table = cfg.getTable();

        loadDriver();
    }

    // ------------------------------------------------------------
    // 1. Load JDBC driver
    // ------------------------------------------------------------
    private void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("[DB] Failed to load MySQL driver: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // 2. Get connection
    // ------------------------------------------------------------
    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }

    // ------------------------------------------------------------
    // 3. Save alert (main insert logic)
    // ------------------------------------------------------------
    public void saveAlert(String topic, LocalDateTime timestamp, String message) {
        String sql = "INSERT INTO " + table + " (topic, timestamp, message) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, topic);
            stmt.setObject(2, timestamp); // LocalDateTime -> DATETIME
            stmt.setString(3, message);

            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("[DB ERROR] Failed to save alert: " + e.getMessage());
        }
    }
}
