package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

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

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAlert(String topic, String timestamp, String message) {

        String sql = "INSERT INTO " + table + " (topic, timestamp, message) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, topic);
            stmt.setString(2, timestamp);
            stmt.setString(3, message);

            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("[DB ERROR] " + e.getMessage());
        }
    }
}
