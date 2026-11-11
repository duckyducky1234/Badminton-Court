package com.ducky.booking.controller.admin;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.controller.BaseServlet;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.service.CourtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@WebServlet("/admin/courts")
public class AdminCourtServlet extends BaseServlet {
    private final CourtService courtService = new CourtService(new DatabaseConnection());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("courts", courtService.listAll());
            show(request, response, "admin/courts");
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String idParameter = request.getParameter("id");
            Long id = idParameter == null || idParameter.isBlank() ? null : positiveLong(request, "id");
            courtService.save(
                    id,
                    required(request, "name"),
                    positiveLong(request, "hourlyRate"),
                    LocalTime.parse(required(request, "openTime")),
                    LocalTime.parse(required(request, "closeTime")),
                    "true".equals(request.getParameter("active")));
            notice(request, "Đã lưu thông tin sân.");
        } catch (DateTimeParseException exception) {
            error(request, "Giờ mở hoặc đóng cửa không hợp lệ.");
        } catch (BusinessException exception) {
            error(request, exception.getMessage());
        } catch (SQLException exception) {
            throw new ServletException(exception);
        }
        redirect(request, response, "/admin/courts");
    }
}
