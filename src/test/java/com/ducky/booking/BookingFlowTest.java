package com.ducky.booking;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;
import com.ducky.booking.model.entity.Court;
import com.ducky.booking.model.service.BookingService;
import com.ducky.booking.model.service.CourtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "BOOKING_TEST_DB_URL", matches = ".+")
class BookingFlowTest {
    @Test
    void reportsPaymentConfirmsAndChecksInExactlyOnce() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Booking hold = fixture.hold();
            assertEquals(BookingStatus.HELD, hold.getStatus());
            assertEquals(fixture.court.getHourlyRate(), hold.getAmount());
            assertFalse(fixture.available());
            assertThrows(BusinessException.class,
                    () -> fixture.bookings.reportPayment(fixture.accountId + 1, hold.getId()));
            assertThrows(BusinessException.class, () -> fixture.bookings.confirm(hold.getId()));

            Booking reported = fixture.bookings.reportPayment(fixture.accountId, hold.getId());
            assertEquals(BookingStatus.PENDING_CONFIRMATION, reported.getStatus());
            assertThrows(BusinessException.class,
                    () -> fixture.bookings.cancel(fixture.accountId, hold.getId()));

            Booking confirmed = fixture.bookings.confirm(hold.getId());
            assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
            assertNotNull(confirmed.getTicketToken());
            assertThrows(BusinessException.class, () -> fixture.bookings.checkIn(confirmed.getTicketToken()));

            Clock onBookingDate = Clock.fixed(
                    fixture.date.atStartOfDay(BookingService.BOOKING_ZONE).toInstant(),
                    BookingService.BOOKING_ZONE);
            BookingService checkIn = new BookingService(fixture.database, onBookingDate);
            Booking checkedIn = checkIn.checkIn(confirmed.getTicketToken());
            assertEquals(BookingStatus.CHECKED_IN, checkedIn.getStatus());
            assertThrows(BusinessException.class, () -> checkIn.checkIn(confirmed.getTicketToken()));
            assertFalse(fixture.available());
            assertEquals(BookingStatus.CHECKED_IN, fixture.bookings.history(fixture.accountId).getFirst().getStatus());
        }
    }

    @Test
    void expiredHoldRemainsInHistoryAndCanBeRebooked() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Booking first = fixture.hold();
            try (Connection connection = fixture.database.open();
                 PreparedStatement statement = connection.prepareStatement("""
                         UPDATE bookings SET expires_at = statement_timestamp() - INTERVAL '1 second' WHERE id = ?
                         """)) {
                statement.setLong(1, first.getId());
                assertEquals(1, statement.executeUpdate());
            }
            assertTrue(fixture.available());
            assertThrows(BusinessException.class,
                    () -> fixture.bookings.reportPayment(fixture.accountId, first.getId()));
            assertEquals(BookingStatus.EXPIRED,
                    fixture.bookings.findOwned(fixture.accountId, first.getId()).getStatus());

            Booking replacement = fixture.hold();
            assertEquals(BookingStatus.CANCELLED,
                    fixture.bookings.cancel(fixture.accountId, replacement.getId()).getStatus());
            assertTrue(fixture.available());
            assertEquals(2, fixture.bookings.history(fixture.accountId).size());
        }
    }

    static final class Fixture implements AutoCloseable {
        private static final LocalTime START = LocalTime.of(8, 0);
        final DatabaseConnection database = new DatabaseConnection(
                System.getenv("BOOKING_TEST_DB_URL"),
                System.getenv("BOOKING_TEST_DB_USER"),
                System.getenv("BOOKING_TEST_DB_PASSWORD"));
        final BookingService bookings = new BookingService(database);
        final LocalDate date = LocalDate.now(BookingService.BOOKING_ZONE).plusDays(1);
        final Court court;
        final long accountId;
        private final List<Long> accountIds = new ArrayList<>();

        Fixture() throws SQLException, BusinessException {
            accountId = createAccount();
            court = new CourtService(database).save(
                    null, UUID.randomUUID().toString(), 80000L, START, START.plusHours(1), true);
        }

        long createAccount() throws SQLException {
            String testId = UUID.randomUUID().toString();
            try (Connection connection = database.open();
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO accounts (name, email, password_hash, role)
                         VALUES ('Booking test', ?, 'test-only', 'USER') RETURNING id
                         """)) {
                statement.setString(1, testId + "@example.invalid");
                try (ResultSet rows = statement.executeQuery()) {
                    rows.next();
                    long id = rows.getLong(1);
                    accountIds.add(id);
                    return id;
                }
            }
        }

        Booking hold() throws SQLException, BusinessException {
            return hold(accountId);
        }

        Booking hold(long account) throws SQLException, BusinessException {
            return bookings.hold(account, court.getId(), date, START);
        }

        boolean available() throws SQLException, BusinessException {
            return bookings.schedule(date).stream()
                    .filter(slot -> slot.getCourt().getId() == court.getId())
                    .findFirst()
                    .orElseThrow()
                    .isAvailable();
        }

        @Override
        public void close() throws SQLException {
            try (Connection connection = database.open();
                 PreparedStatement bookings = connection.prepareStatement("DELETE FROM bookings WHERE court_id = ?");
                 PreparedStatement courts = connection.prepareStatement("DELETE FROM courts WHERE id = ?");
                 PreparedStatement accounts = connection.prepareStatement("DELETE FROM accounts WHERE id = ?")) {
                bookings.setLong(1, court.getId());
                bookings.executeUpdate();
                courts.setLong(1, court.getId());
                courts.executeUpdate();
                for (long id : accountIds) {
                    accounts.setLong(1, id);
                    accounts.executeUpdate();
                }
            }
        }
    }
}
