package com.ducky.booking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private final String url;
    private final String user;
    private final String password;

    static {
        try {
            // Tomcat's initial JDBC discovery cannot see libraries inside the WAR.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    public DatabaseConnection() {
        this(AppConfig.get("DB_URL"), AppConfig.get("DB_USER"), System.getenv("DB_PASSWORD"));
    }

    public DatabaseConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection open() throws SQLException {
        if (url == null || !url.startsWith("jdbc:postgresql:") || user == null || user.isBlank()
                || password == null || password.isEmpty()) {
            throw new SQLException("Cấu hình DB_URL (jdbc:postgresql:...), DB_USER và DB_PASSWORD trước khi kết nối.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
