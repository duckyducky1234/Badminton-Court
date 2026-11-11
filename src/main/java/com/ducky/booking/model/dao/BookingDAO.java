package com.ducky.booking.model.dao;

import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;
import com.ducky.booking.model.entity.Court;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class BookingDAO {
    private static final String SELECT_BOOKING = """
            SELECT b.*, c.name AS court_name,
                   CASE WHEN b.status = 'HELD' AND b.expires_at <= statement_timestamp()
                        THEN 'EXPIRED' ELSE b.status END AS effective_status
            FROM bookings b
            JOIN courts c ON c.id = b.court_id
            """;

    private static final String RETURN_BOOKING = """
            RETURNING bookings.*, status AS effective_status,
                      (SELECT name FROM courts WHERE id = bookings.court_id) AS court_name
            """;

    public Set<ReservedSlot> listOccupied(Connection connection, LocalDate date) throws SQLException {
        String sql = """
                SELECT court_id, start_time FROM bookings
                WHERE booking_date = ?
                  AND (status IN ('PENDING_CONFIRMATION', 'CONFIRMED', 'CHECKED_IN')
                       OR (status = 'HELD' AND expires_at > statement_timestamp()))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, date);
            try (ResultSet rows = statement.executeQuery()) {
                Set<ReservedSlot> slots = new HashSet<>();
                while (rows.next()) {
                    slots.add(new ReservedSlot(rows.getLong("court_id"),
                            rows.getObject("start_time", LocalTime.class)));
                }
                return slots;
            }
        }
    }

    public void expireHold(Connection connection, long courtId, LocalDate date, LocalTime startTime)
            throws SQLException {
        String sql = """
                UPDATE bookings SET status = 'EXPIRED'
                WHERE court_id = ? AND booking_date = ? AND start_time = ?
                  AND status = 'HELD' AND expires_at <= clock_timestamp()
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, courtId);
            statement.setObject(2, date);
            statement.setObject(3, startTime);
            statement.executeUpdate();
        }
    }

    public Optional<Booking> insertHold(Connection connection, long accountId, Court court, LocalDate date,
                                        LocalTime startTime, OffsetDateTime slotStartsAt) throws SQLException {
        String sql = """
                INSERT INTO bookings (account_id, court_id, booking_date, start_time, amount, status, expires_at)
                SELECT ?, ?, ?, ?, ?, 'HELD',
                       LEAST(statement_timestamp() + INTERVAL '5 minutes', slot_starts_at)
                FROM (VALUES (?::timestamptz)) AS slot (slot_starts_at)
                WHERE slot_starts_at > statement_timestamp()
                ON CONFLICT (court_id, booking_date, start_time)
                    WHERE status IN ('HELD', 'PENDING_CONFIRMATION', 'CONFIRMED', 'CHECKED_IN')
                DO NOTHING
                """ + RETURN_BOOKING;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.setLong(2, court.getId());
            statement.setObject(3, date);
            statement.setObject(4, startTime);
            statement.setLong(5, court.getHourlyRate());
            statement.setObject(6, slotStartsAt);
            return readOne(statement);
        }
    }

    public Optional<Booking> findOwned(Connection connection, long accountId, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                SELECT_BOOKING + "WHERE b.id = ? AND b.account_id = ?")) {
            statement.setLong(1, id);
            statement.setLong(2, accountId);
            return readOne(statement);
        }
    }

    public Optional<Booking> findByToken(Connection connection, UUID token) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BOOKING + "WHERE b.ticket_token = ?")) {
            statement.setObject(1, token);
            return readOne(statement);
        }
    }

    public List<Booking> list(Connection connection, Long accountId) throws SQLException {
        String sql = SELECT_BOOKING + (accountId == null ? "" : "WHERE b.account_id = ? ") + "ORDER BY b.id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (accountId != null) {
                statement.setLong(1, accountId);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rows.next()) {
                    bookings.add(mapBooking(rows));
                }
                return bookings;
            }
        }
    }

    public Optional<Booking> changeStatus(Connection connection, long id, Long accountId,
                                          BookingStatus from, BookingStatus to, UUID token)
            throws SQLException {
        String sql = """
                UPDATE bookings
                SET status = ?, ticket_token = COALESCE(?, ticket_token)
                WHERE id = ? AND account_id = COALESCE(?, account_id) AND status = ?
                  AND (status <> 'HELD' OR expires_at > clock_timestamp())
                """ + RETURN_BOOKING;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, to.name());
            statement.setObject(2, token, Types.OTHER);
            statement.setLong(3, id);
            statement.setObject(4, accountId, Types.BIGINT);
            statement.setString(5, from.name());
            return readOne(statement);
        }
    }

    private Optional<Booking> readOne(PreparedStatement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery()) {
            return rows.next() ? Optional.of(mapBooking(rows)) : Optional.empty();
        }
    }

    private Booking mapBooking(ResultSet row) throws SQLException {
        return new Booking(row.getLong("id"), row.getLong("account_id"), row.getLong("court_id"),
                row.getString("court_name"), row.getObject("booking_date", LocalDate.class),
                row.getObject("start_time", LocalTime.class), row.getLong("amount"),
                BookingStatus.valueOf(row.getString("effective_status")),
                row.getObject("expires_at", OffsetDateTime.class).toInstant(),
                row.getObject("ticket_token", UUID.class));
    }

    public record ReservedSlot(long courtId, LocalTime startTime) {
    }
}
