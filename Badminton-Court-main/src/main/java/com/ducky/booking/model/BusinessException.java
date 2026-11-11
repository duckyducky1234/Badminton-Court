package com.ducky.booking.model;

// Lỗi dùng cho các quy tắc nghiệp vụ của hệ thống
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // Tạo lỗi chỉ có thông báo cho người dùng
    public BusinessException(String message) {
        super(message);
    }

    // Tạo lỗi có thông báo và giữ lại lỗi gốc để dễ kiểm tra
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}