package com.ducky.booking.controller.admin;

import com.ducky.booking.controller.BaseServlet;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.service.TicketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/checkin")
public class CheckInServlet extends BaseServlet {
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response, "admin/checkin");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            Booking booking = ticketService.checkIn(required(request, "token"));
            notice(request, "Đã check-in vé #" + booking.getId() + " — " + booking.getCourtName()
                    + ", " + booking.getStartTime() + "–" + booking.getEndTime() + ".");
        } catch (BusinessException exception) {
            error(request, exception.getMessage());
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
        redirect(request, response, "/admin/checkin");
    }
}
