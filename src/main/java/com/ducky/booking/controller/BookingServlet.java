package com.ducky.booking.controller;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;
import com.ducky.booking.model.service.BookingService;
import com.ducky.booking.model.service.PaymentQrService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@WebServlet({"/booking", "/booking/history", "/booking/payment", "/booking/payment-qr", "/booking/cancel"})
public class BookingServlet extends BaseServlet {
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/uuuu")
            .withZone(BookingService.BOOKING_ZONE);

    private final BookingService bookingService = new BookingService(new DatabaseConnection());
    private final PaymentQrService paymentService = new PaymentQrService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long accountId = account(request).getId();
        try {
            switch (request.getServletPath()) {
                case "/booking/history" -> {
                    request.setAttribute("bookings", bookingService.history(accountId));
                    show(request, response, "history");
                }
                case "/booking/payment" -> {
                    Booking booking = bookingService.findOwned(accountId, positiveLong(request, "id"));
                    request.setAttribute("booking", booking);
                    request.setAttribute("payment", paymentService);
                    request.setAttribute("transferReference", paymentService.transferReference(booking));
                    request.setAttribute("paymentDeadline", DEADLINE_FORMAT.format(booking.getExpiresAt()));
                    show(request, response, "payment");
                }
                case "/booking/payment-qr" -> {
                    Booking booking = bookingService.findOwned(accountId, positiveLong(request, "id"));
                    if (booking.getStatus() != BookingStatus.HELD) {
                        throw new BusinessException("Lượt đặt không còn ở trạng thái giữ chỗ.");
                    }
                    sendPng(response, paymentService.render(booking));
                }
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (BusinessException exception) {
            if ("/booking/payment-qr".equals(request.getServletPath())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                error(request, exception.getMessage());
                redirect(request, response, "/booking/history");
            }
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long accountId = account(request).getId();
        String fallback = "/booking/history";
        try {
            switch (request.getServletPath()) {
                case "/booking" -> {
                    fallback = "/schedule";
                    LocalDate date = LocalDate.parse(required(request, "date"));
                    fallback += "?date=" + date;
                    LocalTime start = LocalTime.parse(required(request, "start"));
                    Booking booking = bookingService.hold(accountId, positiveLong(request, "courtId"), date, start);
                    redirect(request, response, "/booking/payment?id=" + booking.getId());
                    return;
                }
                case "/booking/payment" -> {
                    if (!paymentService.isConfigured()) {
                        throw new BusinessException("Mã QR thanh toán chưa được cấu hình. Vui lòng liên hệ quản lý sân.");
                    }
                    bookingService.reportPayment(accountId, positiveLong(request, "id"));
                    notice(request, "Đã gửi thông báo chuyển khoản. Vui lòng chờ quản lý xác nhận.");
                }
                case "/booking/cancel" -> {
                    bookingService.cancel(accountId, positiveLong(request, "id"));
                    notice(request, "Đã hủy lượt đặt sân.");
                }
                default -> {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
            }
        } catch (DateTimeParseException exception) {
            error(request, "Ngày hoặc giờ đặt sân không hợp lệ.");
        } catch (BusinessException exception) {
            error(request, exception.getMessage());
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
        redirect(request, response, fallback);
    }
}
