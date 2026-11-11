package com.ducky.booking.setup;

import com.ducky.booking.config.AppConfig;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.service.AuthService;

import java.sql.SQLException;

public final class BootstrapAdmin {
    private BootstrapAdmin() {}

    public static void main(String[] args) throws SQLException {
        String name = AppConfig.get("BOOKING_ADMIN_NAME");
        String email = AppConfig.get("BOOKING_ADMIN_EMAIL");
        String password = System.getenv("BOOKING_ADMIN_PASSWORD");
        if (name == null || email == null || password == null) {
            System.err.println("Cần đặt BOOKING_ADMIN_NAME, BOOKING_ADMIN_EMAIL và BOOKING_ADMIN_PASSWORD.");
            System.exit(1);
        }

        try {
            var admin = new AuthService().createAdmin(name, email, password);
            System.out.println("Đã tạo tài khoản quản trị " + admin.getEmail());
        } catch (BusinessException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
