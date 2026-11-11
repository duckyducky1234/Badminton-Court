package com.ducky.booking.model.service;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.dao.BookingDAO;
import com.ducky.booking.model.dao.BookingDAO.ReservedSlot;
import com.ducky.booking.model.dao.CourtDAO;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;
import com.ducky.booking.model.entity.Court;
import com.ducky.booking.model.entity.CourtHours;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BookingService {
    public static final ZoneId BOOKING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final DatabaseConnection database;
    private final BookingDAO bookings = new BookingDAO();
    private final CourtDAO courts = new CourtDAO();
    private final Clock clock;

    public BookingService(DatabaseConnection database) {
        this(database, Clock.system(BOOKING_ZONE));
    }

    public BookingService(DatabaseConnection database, Clock clock) {
        this.database = database;
        this.clock = clock.withZone(BOOKING_ZONE);
    }

    public List<Slot> schedule(LocalDate date) throws SQLException, BusinessException {
        validateDate(date);
        try (Connection connection = database.open()) {
            List<Court> activeCourts = courts.list(connection, true);
            Set<ReservedSlot> occupied = bookings.listOccupied(connection, date);
            LocalDateTime now = LocalDateTime.now(clock);
            List<Slot> slots = new ArrayList<>();
            for (Court court : activeCourts) {
                for (LocalTime start = court.getOpenTime(); start.isBefore(court.getCloseTime());
                     start = start.plusHours(1)) {
                    boolean available = date.atTime(start).isAfter(now)
                            && !occupied.contains(new ReservedSlot(court.getId(), start));
                    slots.add(new Slot(court, date, start, available));
                }
            }
            return slots;
        }
    }

    public Booking hold(long accountId, long courtId, LocalDate date, LocalTime startTime)
            throws SQLException, BusinessException {
        validateDate(date);
        if (!CourtHours.isWholeHour(startTime)) {
            throw new BusinessException("Vui lòng chọn khung giờ bắt đầu vào giờ tròn.");
        }
        LocalDateTime slotStart = date.atTime(startTime);

        try (Connection connection = database.open()) {
            connection.setAutoCommit(false);
            try {
                // Serialize court edits and reservations for this court until the hold commits.
                Court court = courts.lock(connection, courtId)
                        .filter(Court::isActive)
                        .orElseThrow(() -> new BusinessException("Sân hiện không nhận đặt chỗ."));
                if (!court.getHours().containsSlot(startTime)) {
                    throw new BusinessException("Khung giờ nằm ngoài giờ mở cửa của sân.");
                }
                if (!slotStart.isAfter(LocalDateTime.now(clock))) {
                    throw new BusinessException("Khung giờ này đã bắt đầu hoặc đã qua.");
                }

                bookings.expireHold(connection, courtId, date, startTime);
                Booking booking = bookings.insertHold(connection, accountId, court, date, startTime,
                                slotStart.atZone(BOOKING_ZONE).toOffsetDateTime())
                        .orElseThrow(() -> new BusinessException("Khung giờ này hiện không còn trống."));
                connection.commit();
                return booking;
            } catch (BusinessException | SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            }
        }
    }

    public Booking reportPayment(long accountId, long id) throws SQLException, BusinessException {
        return transition(id, accountId, BookingStatus.HELD, BookingStatus.PENDING_CONFIRMATION, null,
                "Chỉ có thể báo chuyển khoản khi lượt giữ chỗ còn hiệu lực.");
    }

    public Booking cancel(long accountId, long id) throws SQLException, BusinessException {
        return transition(id, accountId, BookingStatus.HELD, BookingStatus.CANCELLED, null,
                "Chỉ có thể hủy lượt giữ chỗ còn hiệu lực trước khi báo chuyển khoản.");
    }

    public Booking confirm(long id) throws SQLException, BusinessException {
        return transition(id, null, BookingStatus.PENDING_CONFIRMATION, BookingStatus.CONFIRMED,
                UUID.randomUUID(), "Chỉ có thể duyệt lượt đặt đang chờ xác nhận.");
    }

    public Booking reject(long id) throws SQLException, BusinessException {
        return transition(id, null, BookingStatus.PENDING_CONFIRMATION, BookingStatus.REJECTED, null,
                "Chỉ có thể từ chối lượt đặt đang chờ xác nhận.");
    }

    public Booking checkIn(UUID token) throws SQLException, BusinessException {
        try (Connection connection = database.open()) {
            Booking booking = bookings.findByToken(connection, token)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy vé."));
            if (booking.getStatus() == BookingStatus.CHECKED_IN) {
                throw new BusinessException("Vé này đã được check-in.");
            }
            if (!LocalDate.now(clock).equals(booking.getDate())) {
                throw new BusinessException("Chỉ có thể check-in vào ngày đặt sân.");
            }
            return bookings.changeStatus(connection, booking.getId(), null, BookingStatus.CONFIRMED,
                            BookingStatus.CHECKED_IN, null)
                    .orElseThrow(() -> new BusinessException("Vé này đã được check-in."));
        }
    }

    public Booking findOwned(long accountId, long id) throws SQLException, BusinessException {
        try (Connection connection = database.open()) {
            return bookings.findOwned(connection, accountId, id)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy lượt đặt sân."));
        }
    }

    public List<Booking> history(long accountId) throws SQLException {
        return list(accountId);
    }

    public List<Booking> listAll() throws SQLException {
        return list(null);
    }

    private List<Booking> list(Long accountId) throws SQLException {
        try (Connection connection = database.open()) {
            return bookings.list(connection, accountId);
        }
    }

    private Booking transition(long id, Long accountId, BookingStatus from, BookingStatus to,
                               UUID token, String message) throws SQLException, BusinessException {
        try (Connection connection = database.open()) {
            return bookings.changeStatus(connection, id, accountId, from, to, token)
                    .orElseThrow(() -> new BusinessException(message));
        }
    }

    private void validateDate(LocalDate date) throws BusinessException {
        if (date == null || date.getYear() < 1 || date.getYear() > 9999) {
            throw new BusinessException("Ngày đặt sân không hợp lệ.");
        }
    }

    public static final class Slot {
        private final Court court;
        private final LocalDate date;
        private final LocalTime startTime;
        private final boolean available;

        private Slot(Court court, LocalDate date, LocalTime startTime, boolean available) {
            this.court = court;
            this.date = date;
            this.startTime = startTime;
            this.available = available;
        }

        public Court getCourt() {
            return court;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return startTime.plusHours(1);
        }

        public boolean isAvailable() {
            return available;
        }
    }
}
