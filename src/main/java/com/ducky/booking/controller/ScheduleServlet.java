package com.ducky.booking.controller;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.service.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/schedule")
public class ScheduleServlet extends BaseServlet {
    private final BookingService bookingService = new BookingService(new DatabaseConnection());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LocalDate today = LocalDate.now(BookingService.BOOKING_ZONE);
        String dateParameter = request.getParameter("date");
        try {
            LocalDate date = dateParameter == null || dateParameter.isBlank() ? today : LocalDate.parse(dateParameter);
            request.setAttribute("selectedDate", date);
            request.setAttribute("slots", bookingService.schedule(date));
        } catch (DateTimeParseException exception) {
            request.setAttribute("selectedDate", today);
            request.setAttribute("error", "Ngày được chọn không hợp lệ.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (BusinessException exception) {
            request.setAttribute("error", exception.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
        show(request, response, "schedule");
    }
}
