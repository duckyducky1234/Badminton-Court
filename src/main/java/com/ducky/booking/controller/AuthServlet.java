package com.ducky.booking.controller;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Account;
import com.ducky.booking.model.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet({"/auth/login", "/auth/register", "/auth/logout"})
public class AuthServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/auth/login" -> show(request, response, "login");
            case "/auth/register" -> show(request, response, "register");
            default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/auth/logout".equals(request.getServletPath())) {
            request.getSession().invalidate();
            redirect(request, response, "/home");
            return;
        }

        boolean register = "/auth/register".equals(request.getServletPath());
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        try {
            Account account = register ? authService.register(name, email, password)
                    : authService.login(email, password);
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.setAttribute(ACCOUNT_ATTRIBUTE, account);
            redirect(request, response, account.isAdmin() ? "/admin/bookings" : "/schedule");
        } catch (BusinessException exception) {
            request.setAttribute("error", exception.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("name", name);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            show(request, response, register ? "register" : "login");
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
    }
}
