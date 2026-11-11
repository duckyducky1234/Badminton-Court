package com.ducky.booking.model.entity;

import java.time.LocalTime;

public record CourtHours(LocalTime openTime, LocalTime closeTime) {
    public CourtHours {
        if (!isWholeHour(openTime) || !isWholeHour(closeTime) || !openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Giờ mở và đóng cửa phải tròn giờ trong cùng ngày.");
        }
    }

    public static boolean isWholeHour(LocalTime time) {
        return time != null && time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    public boolean containsSlot(LocalTime startTime) {
        return isWholeHour(startTime) && !startTime.isBefore(openTime) && startTime.isBefore(closeTime);
    }
}
