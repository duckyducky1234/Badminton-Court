package com.ducky.booking.controller;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.service.TicketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/ticket/qr")
public class TicketServlet extends BaseServlet {
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            sendPng(response, ticketService.render(account(request).getId(), positiveLong(request, "id")));
        } catch (BusinessException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
    }
}
