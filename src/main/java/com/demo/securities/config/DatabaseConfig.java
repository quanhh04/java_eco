package com.demo.securities.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        Path configFile = Path.of("db.properties");
        try (InputStream in = Files.newInputStream(configFile)) {
            PROPERTIES.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được file cấu hình db.properties", e);
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Không tìm thấy driver PostgreSQL trên classpath", e);
        }
    }

    private DatabaseConfig() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.user"),
                PROPERTIES.getProperty("db.password"));
    }

    public static String getSchema() {
        return PROPERTIES.getProperty("db.schema", "public");
    }
}
