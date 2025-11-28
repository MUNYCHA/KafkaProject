package org.munycha.kafkaconsumer.config;

public class DatabaseConfig {

    private String url;
    private String user;
    private String password;
    private String table;  // NEW

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public String getTable() { return table; }  // NEW
}
