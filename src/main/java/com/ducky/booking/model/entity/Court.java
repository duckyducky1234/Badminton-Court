package com.ducky.booking.model.entity;

import java.time.LocalTime;

public final class Court {
    private final long id;
    private final String name;
    private final long hourlyRate;
    private final CourtHours hours;
    private final boolean active;

    public Court(long id, String name, long hourlyRate, CourtHours hours, boolean active) {
        this.id = id;
        this.name = name;
        this.hourlyRate = hourlyRate;
        this.hours = hours;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getHourlyRate() {
        return hourlyRate;
    }

    public CourtHours getHours() {
        return hours;
    }

    public LocalTime getOpenTime() {
        return hours.openTime();
    }

    public LocalTime getCloseTime() {
        return hours.closeTime();
    }

    public boolean isActive() {
        return active;
    }
}
