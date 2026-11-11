package com.ducky.booking.model.dao;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.entity.Account;
import com.ducky.booking.model.entity.Admin;
import com.ducky.booking.model.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class AccountDAO {
    private final DatabaseConnection database;

    public AccountDAO(DatabaseConnection database) {
        this.database = database;
    }

    public Account insert(String name, String email, String passwordHash, String role) throws SQLException {
        String sql = """
                INSERT INTO accounts (name, email, password_hash, role)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (email) DO NOTHING
                RETURNING id, name, email, role
                """;
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, role);
            try (var result = statement.executeQuery()) {
                return result.next() ? mapAccount(result) : null;
            }
        }
    }

    public Credentials findCredentials(String email) throws SQLException {
        String sql = "SELECT id, name, email, role, password_hash FROM accounts WHERE email = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return new Credentials(mapAccount(result), result.getString("password_hash"));
                }
                return null;
            }
        }
    }

    private Account mapAccount(ResultSet result) throws SQLException {
        long id = result.getLong("id");
        String name = result.getString("name");
        String email = result.getString("email");
        return switch (result.getString("role")) {
            case "ADMIN" -> new Admin(id, name, email);
            case "USER" -> new User(id, name, email);
            default -> throw new SQLException("Invalid account role in database");
        };
    }

    public record Credentials(Account account, String passwordHash) {}
}
