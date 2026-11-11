package com.ducky.booking.controller.admin;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.controller.BaseServlet;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.service.BookingService;
import com.ducky.booking.model.service.PaymentQrService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/bookings")
public class AdminBookingServlet extends BaseServlet {
    private final BookingService bookingService = new BookingService(new DatabaseConnection());
    private final PaymentQrService paymentService = new PaymentQrService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Booking> bookings = bookingService.listAll();
            Map<Long, String> references = new HashMap<>();
            for (Booking booking : bookings) {
                references.put(booking.getId(), paymentService.transferReference(booking));
            }
            request.setAttribute("bookings", bookings);
            request.setAttribute("references", references);
            show(request, response, "admin/bookings");
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            long id = positiveLong(request, "id");
            switch (required(request, "action")) {
                case "confirm" -> {
                    bookingService.confirm(id);
                    notice(request, "Đã xác nhận chuyển khoản và phát hành vé.");
                }
                case "reject" -> {
                    bookingService.reject(id);
                    notice(request, "Đã từ chối lượt đặt.");
                }
                default -> throw new BusinessException("Thao tác không hợp lệ.");
            }
        } catch (BusinessException exception) {
            error(request, exception.getMessage());
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
        redirect(request, response, "/admin/bookings");
    }
}
