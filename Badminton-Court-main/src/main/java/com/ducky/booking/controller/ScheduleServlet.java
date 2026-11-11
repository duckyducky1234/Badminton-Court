package com.ducky.booking.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Nhận yêu cầu truy cập đường dẫn /schedule
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Dùng múi giờ Việt Nam để xác định ngày hiện tại
    private static final ZoneId VIETNAM_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        // Lấy tham số date từ URL hoặc form chọn ngày
        String dateParam = request.getParameter("date");

        // Mặc định chọn ngày hôm nay nếu chưa có tham số date
        LocalDate selectedDate = LocalDate.now(VIETNAM_ZONE);

        if (dateParam != null && !dateParam.isBlank()) {
            try {
                // Chuyển chuỗi yyyy-MM-dd thành đối tượng ngày
                selectedDate = LocalDate.parse(dateParam.trim());

            } catch (DateTimeParseException exception) {
                // Ngày không hợp lệ: báo lỗi và giữ ngày mặc định
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                request.setAttribute(
                        "errorMessage",
                        "Ngày không hợp lệ. Vui lòng nhập ngày "
                                + "theo định dạng yyyy-MM-dd."
                );
            }
        }

        // Gửi dữ liệu sang giao diện JSP
        request.setAttribute("activePage", "schedule");
        request.setAttribute("selectedDate", selectedDate.toString());

        // Hiển thị trang lịch sân
        request.getRequestDispatcher("/WEB-INF/interface/schedule.jsp")
                .forward(request, response);
    }
}