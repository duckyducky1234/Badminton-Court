package com.ducky.booking.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.DriverManager;
import java.sql.SQLException;

@WebListener
public final class PostgresDriverLifecycle implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ClassLoader appLoader = getClass().getClassLoader();
        DriverManager.drivers()
                .filter(driver -> driver.getClass().getClassLoader() == appLoader)
                .forEach(driver -> {
                    try {
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException exception) {
                        event.getServletContext().log("Không thể hủy đăng ký JDBC driver.", exception);
                    }
                });
    }
}
