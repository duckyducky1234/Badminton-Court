package com.ducky.booking.model.service;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;

import java.sql.SQLException;
import java.util.UUID;

public final class TicketService {
    private static final String PREFIX = "BADMINTON:";

    private final BookingService bookings;

    public TicketService() {
        this(new BookingService(new DatabaseConnection()));
    }

    public TicketService(BookingService bookings) {
        this.bookings = bookings;
    }

    public byte[] render(long accountId, long bookingId) throws SQLException, BusinessException {
        Booking booking = bookings.findOwned(accountId, bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED || booking.getTicketToken() == null) {
            throw new BusinessException("Vé QR chỉ có hiệu lực sau khi quản lý duyệt và trước khi check-in.");
        }
        return QrImage.png(PREFIX + booking.getTicketToken());
    }

    public Booking checkIn(String scannedValue) throws SQLException, BusinessException {
        String value = scannedValue == null ? "" : scannedValue.strip();
        if (value.startsWith(PREFIX)) {
            value = value.substring(PREFIX.length());
        }
        UUID token;
        try {
            token = UUID.fromString(value);
            if (!token.toString().equalsIgnoreCase(value)) {
                throw new IllegalArgumentException("Invalid ticket format");
            }
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Mã vé QR không hợp lệ.");
        }
        return bookings.checkIn(token);
    }
}
