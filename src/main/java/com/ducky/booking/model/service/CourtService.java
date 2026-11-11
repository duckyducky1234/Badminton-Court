package com.ducky.booking.model.service;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.dao.CourtDAO;
import com.ducky.booking.model.entity.Court;
import com.ducky.booking.model.entity.CourtHours;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public final class CourtService {
    private final DatabaseConnection database;
    private final CourtDAO courts = new CourtDAO();

    public CourtService(DatabaseConnection database) {
        this.database = database;
    }

    public List<Court> listActive() throws SQLException {
        return list(true);
    }

    public List<Court> listAll() throws SQLException {
        return list(false);
    }

    private List<Court> list(boolean activeOnly) throws SQLException {
        try (Connection connection = database.open()) {
            return courts.list(connection, activeOnly);
        }
    }

    public Court save(Long id, String name, long hourlyRate, LocalTime openTime, LocalTime closeTime,
                      boolean active) throws SQLException, BusinessException {
        String trimmedName = name == null ? "" : name.strip();
        if (trimmedName.isBlank() || trimmedName.codePointCount(0, trimmedName.length()) > 100) {
            throw new BusinessException("Tên sân phải có từ 1 đến 100 ký tự.");
        }
        if (hourlyRate <= 0 || hourlyRate >= 500_000_000) {
            throw new BusinessException("Giá sân theo giờ phải từ 1 đến 499.999.999 đồng.");
        }
        CourtHours hours;
        try {
            hours = new CourtHours(openTime, closeTime);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }

        try (Connection connection = database.open()) {
            return courts.save(connection, id, trimmedName, hourlyRate, hours, active)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy sân."));
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new BusinessException("Tên sân này đã được sử dụng.");
            }
            throw e;
        }
    }
}
