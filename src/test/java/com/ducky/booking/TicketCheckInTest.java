package com.ducky.booking;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.BookingStatus;
import com.ducky.booking.model.service.BookingService;
import com.ducky.booking.model.service.TicketService;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketCheckInTest {
    @Test
    void rejectsMalformedCodesBeforeLookingUpATicket() {
        var tickets = new TicketService();

        assertThrows(BusinessException.class, () -> tickets.checkIn("not a ticket"));
        assertThrows(BusinessException.class, () -> tickets.checkIn("BADMINTON:1-1-1-1-1"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "BOOKING_TEST_DB_URL", matches = ".+")
    void onlyTheOwnerCanGetAnApprovedTicketAndItCanBeUsedOnce() throws Exception {
        try (var fixture = new BookingFlowTest.Fixture()) {
            Clock checkInClock = Clock.fixed(fixture.date.atStartOfDay(BookingService.BOOKING_ZONE).toInstant(),
                    BookingService.BOOKING_ZONE);
            var tickets = new TicketService(new BookingService(fixture.database, checkInClock));
            var hold = fixture.hold();
            long bookingId = hold.getId();

            assertThrows(BusinessException.class, () -> tickets.render(fixture.accountId, bookingId));
            fixture.bookings.reportPayment(fixture.accountId, bookingId);
            var confirmed = fixture.bookings.confirm(bookingId);
            assertThrows(BusinessException.class, () -> tickets.render(fixture.accountId + 1, bookingId));

            byte[] png = tickets.render(fixture.accountId, bookingId);
            var image = ImageIO.read(new ByteArrayInputStream(png));
            var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            String code = new QRCodeReader().decode(bitmap).getText();
            assertEquals("BADMINTON:" + confirmed.getTicketToken(), code);

            assertEquals(BookingStatus.CHECKED_IN, tickets.checkIn(code).getStatus());
            assertEquals(BookingStatus.CHECKED_IN,
                    fixture.bookings.findOwned(fixture.accountId, bookingId).getStatus());
            assertThrows(BusinessException.class, () -> tickets.checkIn(confirmed.getTicketToken().toString()));
            assertThrows(BusinessException.class, () -> tickets.render(fixture.accountId, bookingId));
        }
    }
}
