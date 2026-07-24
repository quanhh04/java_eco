package com.demo.securities.tool;

import com.demo.securities.config.DatabaseConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    public static void main(String[] args) throws IOException, SQLException {
        String sql = Files.readString(Path.of("schema.sql"));
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.strip();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
        System.out.println("Da khoi tao schema thanh cong.");
    }
}
