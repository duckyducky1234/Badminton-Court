package com.ducky.booking.model.entity;

public enum BookingStatus {
    HELD("Đang giữ chỗ"),
    PENDING_CONFIRMATION("Chờ xác nhận chuyển khoản"),
    CONFIRMED("Đã xác nhận"),
    CHECKED_IN("Đã check-in"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy"),
    EXPIRED("Hết hạn giữ chỗ");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
