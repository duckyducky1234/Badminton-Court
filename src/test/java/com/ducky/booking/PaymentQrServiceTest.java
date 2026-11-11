package com.ducky.booking;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;
import com.ducky.booking.model.service.PaymentQrService;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentQrServiceTest {
    private final PaymentQrService payment = new PaymentQrService("970000", "00000000000", "Test");

    @Test
    void rendersTheVietQrBeneficiaryAmountReferenceAndChecksum() throws Exception {
        byte[] png = payment.render(booking(BookingStatus.HELD));
        var image = ImageIO.read(new ByteArrayInputStream(png));
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        String payload = new QRCodeReader().decode(bitmap).getText();

        assertEquals("00020101021238550010A00000072701250006970000011100000000000"
                + "0208QRIBFTTA530370454061800005802VN62100806SAN 426304E906", payload);
    }

    @Test
    void rejectsRequestsWithoutARecipientOrAnActiveHold() {
        var unconfigured = new PaymentQrService(null, null, null);

        assertThrows(BusinessException.class, () -> unconfigured.render(booking(BookingStatus.HELD)));
        assertThrows(BusinessException.class, () -> payment.render(booking(BookingStatus.EXPIRED)));
    }

    private Booking booking(BookingStatus status) {
        return new Booking(42L, 1L, 1L, "Sân 1", LocalDate.of(2026, 1, 1),
                LocalTime.of(9, 0), 180_000L, status, Instant.parse("2026-01-01T01:00:00Z"), null);
    }
}
