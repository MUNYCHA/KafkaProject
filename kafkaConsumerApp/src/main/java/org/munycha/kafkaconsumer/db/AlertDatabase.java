package org.munycha.kafkaconsumer.db;

import org.munycha.kafkaconsumer.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

/**
 * AlertDatabase handles saving alert messages to a relational database.
 * It connects to the database using JDBC and inserts alert records into the configured table.
 */
public class AlertDatabase {

    // Database connection configuration
    private final String url;
    private final String user;
    private final String password;
    private final String table;

    /**
     * Constructs an AlertDatabase instance using the provided DatabaseConfig.
     *
     * @param dbConfig configuration object containing JDBC connection details
     */
    public AlertDatabase(DatabaseConfig dbConfig) {
        this.url = dbConfig.getUrl();
        this.user = dbConfig.getUser();
        this.password = dbConfig.getPassword();
        this.table = dbConfig.getTable();

        loadDriver(); // Ensure MySQL driver is loaded
    }

    // ------------------------------------------------------------
    // 1. Load JDBC driver
    // ------------------------------------------------------------

    /**
     * Loads the MySQL JDBC driver class. Required for JDBC connections.
     */
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

    /**
     * Establishes and returns a new database connection.
     *
     * @return active JDBC connection
     * @throws Exception if the connection fails
     */
    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }

    // ------------------------------------------------------------
    // 3. Save alert (main insert logic)
    // ------------------------------------------------------------

    /**
     * Inserts an alert record into the database.
     *
     * @param topic     Kafka topic name
     * @param timestamp Alert timestamp
     * @param message   Message content that triggered the alert
     */
    public void saveAlert(String topic, LocalDateTime timestamp, String message) {
        String sql = "INSERT INTO " + table + " (topic, timestamp, message) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, topic);
            stmt.setObject(2, timestamp); // Uses LocalDateTime → SQL DATETIME
            stmt.setString(3, message);

            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("[DB ERROR] Failed to save alert: " + e.getMessage());
        }
    }
}
