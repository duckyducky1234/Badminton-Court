package com.ducky.booking.controller;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    public static final String ACCOUNT_ATTRIBUTE = "account";
    private static final String NOTICE_ATTRIBUTE = "notice";
    private static final String ERROR_ATTRIBUTE = "error";

    protected Account account(HttpServletRequest request) {
        return (Account) request.getAttribute(ACCOUNT_ATTRIBUTE);
    }

    protected void show(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            for (String attribute : new String[]{NOTICE_ATTRIBUTE, ERROR_ATTRIBUTE}) {
                Object value = session.getAttribute(attribute);
                session.removeAttribute(attribute);
                if (value != null && request.getAttribute(attribute) == null) {
                    request.setAttribute(attribute, value);
                }
            }
        }
        request.getRequestDispatcher("/WEB-INF/interface/" + view + ".jsp").forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) {
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", request.getContextPath() + path);
    }

    protected void notice(HttpServletRequest request, String message) {
        request.getSession().setAttribute(NOTICE_ATTRIBUTE, message);
    }

    protected void error(HttpServletRequest request, String message) {
        request.getSession().setAttribute(ERROR_ATTRIBUTE, message);
    }

    protected long positiveLong(HttpServletRequest request, String parameter) throws BusinessException {
        try {
            long value = Long.parseLong(request.getParameter(parameter));
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException exception) {
            // Dùng cùng thông báo cho dữ liệu thiếu hoặc sai định dạng.
        }
        throw new BusinessException("Thông tin gửi lên không hợp lệ.");
    }

    protected String required(HttpServletRequest request, String parameter) throws BusinessException {
        String value = request.getParameter(parameter);
        if (value == null || value.isBlank()) {
            throw new BusinessException("Vui lòng nhập đầy đủ thông tin.");
        }
        return value;
    }

    protected void sendPng(HttpServletResponse response, byte[] png) throws IOException {
        response.setContentType("image/png");
        response.getOutputStream().write(png);
    }
}
