package com.ducky.booking.model.dao;

import com.ducky.booking.model.entity.Court;
import com.ducky.booking.model.entity.CourtHours;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CourtDAO {
    public List<Court> list(Connection connection, boolean activeOnly) throws SQLException {
        String sql = "SELECT * FROM courts WHERE (NOT ? OR active) ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, activeOnly);
            try (ResultSet rows = statement.executeQuery()) {
                List<Court> courts = new ArrayList<>();
                while (rows.next()) {
                    courts.add(mapCourt(rows));
                }
                return courts;
            }
        }
    }

    public Optional<Court> lock(Connection connection, long id) throws SQLException {
        String sql = "SELECT * FROM courts WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return readOne(statement);
        }
    }

    public Optional<Court> save(Connection connection, Long id, String name, long hourlyRate,
                                CourtHours hours, boolean active) throws SQLException {
        String sql = id == null
                ? """
                    INSERT INTO courts (name, hourly_rate, open_time, close_time, active)
                    VALUES (?, ?, ?, ?, ?) RETURNING *
                    """
                : """
                    UPDATE courts SET name = ?, hourly_rate = ?, open_time = ?, close_time = ?, active = ?
                    WHERE id = ? RETURNING *
                    """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setLong(2, hourlyRate);
            statement.setObject(3, hours.openTime());
            statement.setObject(4, hours.closeTime());
            statement.setBoolean(5, active);
            if (id != null) {
                statement.setLong(6, id);
            }
            return readOne(statement);
        }
    }

    private Optional<Court> readOne(PreparedStatement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery()) {
            return rows.next() ? Optional.of(mapCourt(rows)) : Optional.empty();
        }
    }

    private Court mapCourt(ResultSet row) throws SQLException {
        CourtHours hours = new CourtHours(row.getObject("open_time", LocalTime.class),
                row.getObject("close_time", LocalTime.class));
        return new Court(row.getLong("id"), row.getString("name"), row.getLong("hourly_rate"),
                hours, row.getBoolean("active"));
    }
}
