package com.ducky.booking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            System.getenv("DATABASE_URL");

    private static final String USER =
            System.getenv("DATABASE_USER");

    private static final String PASSWORD =
            System.getenv("DATABASE_PASSWORD");

    private DatabaseConnection() {
        // Không cho tạo đối tượng từ class này
    }

    public static Connection getConnection() throws SQLException {
        if (URL == null || URL.isBlank()) {
            throw new SQLException(
                    "Chưa cấu hình DATABASE_URL."
            );
        }

        if (USER == null || USER.isBlank()) {
            throw new SQLException(
                    "Chưa cấu hình DATABASE_USER."
            );
        }

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "Chưa cấu hình DATABASE_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}