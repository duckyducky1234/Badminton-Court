package com.ducky.booking.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.entity.Account;

public class AccountDAO {

    // Tìm tài khoản bằng email để đăng nhập
    public Account findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role
                FROM accounts
                WHERE email = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Account(
                            result.getLong("id"),
                            result.getString("full_name"),
                            result.getString("email"),
                            result.getString("password_hash"),
                            result.getString("role")
                    );
                }
            }
        }

        return null;
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM accounts WHERE email = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Lưu tài khoản mới vào database
    public void create(Account account) throws SQLException {
        String sql = """
                INSERT INTO accounts (
                    full_name,
                    email,
                    password_hash,
                    role
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, account.getFullName());
            statement.setString(2, account.getEmail());
            statement.setString(3, account.getPasswordHash());
            statement.setString(4, account.getRole());

            statement.executeUpdate();
        }
    }
}