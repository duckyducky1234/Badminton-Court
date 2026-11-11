package com.ducky.booking.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Account;
import com.ducky.booking.model.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AuthService authService = new AuthService();

    // GET: mở trang đăng nhập hoặc đăng ký
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String action = request.getPathInfo();

        if ("/login".equals(action)) {
            showPage(request, response, "login");

        } else if ("/register".equals(action)) {
            showPage(request, response, "register");

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // POST: xử lý dữ liệu người dùng gửi lên
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getPathInfo();

        try {
            if ("/login".equals(action)) {
                login(request, response);

            } else if ("/register".equals(action)) {
                register(request, response);

            } else if ("/logout".equals(action)) {
                logout(request, response);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException exception) {
            // Ghi lỗi kỹ thuật vào log của máy chủ
            getServletContext().log(
                    "Database error while processing authentication",
                    exception
            );

            // Không gửi chi tiết database cho người dùng
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Hệ thống đang gặp lỗi. Vui lòng thử lại sau."
            );
        }
    }

    // Xử lý đăng nhập
    private void login(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        String email = readText(request, "email");

        // Giữ nguyên mật khẩu, không loại bỏ khoảng trắng
        String password = request.getParameter("password");

        try {
            // Service kiểm tra dữ liệu và xác thực tài khoản
            Account account = authService.login(email, password);

            // Tạo session nếu chưa có
            HttpSession session = request.getSession();

            // Đổi mã session sau khi đăng nhập thành công
            request.changeSessionId();

            // Chỉ lưu thông tin cần thiết, không lưu mật khẩu
            session.setAttribute("userId", account.getId());
            session.setAttribute("fullName", account.getFullName());
            session.setAttribute("role", account.getRole());

            // Chuyển về trang chủ
            response.sendRedirect(
                    request.getContextPath() + "/home"
            );

        } catch (BusinessException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            request.setAttribute("errorMessage", exception.getMessage());
            request.setAttribute("email", email);

            showPage(request, response, "login");
        }
    }

    // Xử lý đăng ký
    private void register(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        String fullName = readText(request, "fullName");
        String email = readText(request, "email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            // Service kiểm tra thông tin và tạo tài khoản khách hàng
            authService.register(
                    fullName,
                    email,
                    password,
                    confirmPassword
            );

            // Đăng ký thành công thì chuyển tới trang đăng nhập
            response.sendRedirect(
                    request.getContextPath() + "/auth/login?registered=1"
            );

        } catch (BusinessException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            request.setAttribute("errorMessage", exception.getMessage());

            // Giữ lại tên và email để người dùng không phải nhập lại
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);

            showPage(request, response, "register");
        }
    }

    // Xử lý đăng xuất
    private void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        // false: không tạo session mới nếu chưa có
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                request.getContextPath() + "/home"
        );
    }

    // Đọc trường văn bản và bỏ khoảng trắng ở đầu/cuối
    private String readText(
            HttpServletRequest request,
            String parameterName
    ) {
        String value = request.getParameter(parameterName);

        return value == null ? "" : value.trim();
    }

    // Chuyển yêu cầu sang trang JSP tương ứng
    private void showPage(
            HttpServletRequest request,
            HttpServletResponse response,
            String page
    ) throws ServletException, IOException {

        request.setAttribute("activePage", page);

        request.getRequestDispatcher(
                "/WEB-INF/interface/" + page + ".jsp"
        ).forward(request, response);
    }
}