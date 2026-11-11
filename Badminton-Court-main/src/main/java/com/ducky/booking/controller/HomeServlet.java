package com.ducky.booking.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Gắn đường dẫn /home với servlet này
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    // Tomcat gọi doGet khi trình duyệt yêu cầu xem trang chủ
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        // Chuyển request sang JSP để tạo nội dung trang chủ
        request.getRequestDispatcher("/WEB-INF/interface/home.jsp")
                .forward(request, response);
    }
}