package com.ducky.booking;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "BOOKING_TEST_DB_URL", matches = ".+")
class BookingConcurrencyTest {
    @Test
    @Timeout(20)
    void simultaneousRequestsCannotHoldTheSameSlot() throws Exception {
        try (BookingFlowTest.Fixture fixture = new BookingFlowTest.Fixture();
             var executor = Executors.newFixedThreadPool(2)) {
            long anotherAccount = fixture.createAccount();
            CountDownLatch bothReady = new CountDownLatch(2);
            var first = executor.submit(() -> attemptHold(fixture, fixture.accountId, bothReady));
            var second = executor.submit(() -> attemptHold(fixture, anotherAccount, bothReady));

            assertEquals(1, first.get() + second.get());
            assertEquals(1, fixture.bookings.history(fixture.accountId).size()
                    + fixture.bookings.history(anotherAccount).size());
        }
    }

    private int attemptHold(BookingFlowTest.Fixture fixture, long accountId, CountDownLatch bothReady)
            throws SQLException, InterruptedException {
        bothReady.countDown();
        bothReady.await();
        try {
            assertEquals(BookingStatus.HELD, fixture.hold(accountId).getStatus());
            return 1;
        } catch (BusinessException e) {
            assertEquals("Khung giờ này hiện không còn trống.", e.getMessage());
            return 0;
        }
    }
}
