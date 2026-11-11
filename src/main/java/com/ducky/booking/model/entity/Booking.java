package com.ducky.booking.model.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class Booking {
    private final long id;
    private final long accountId;
    private final long courtId;
    private final String courtName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final long amount;
    private final BookingStatus status;
    private final Instant expiresAt;
    private final UUID ticketToken;

    public Booking(long id, long accountId, long courtId, String courtName, LocalDate date,
                   LocalTime startTime, long amount, BookingStatus status, Instant expiresAt,
                   UUID ticketToken) {
        this.id = id;
        this.accountId = accountId;
        this.courtId = courtId;
        this.courtName = courtName;
        this.date = date;
        this.startTime = startTime;
        this.amount = amount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.ticketToken = ticketToken;
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getCourtId() {
        return courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return startTime.plusHours(1);
    }

    public long getAmount() {
        return amount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UUID getTicketToken() {
        return ticketToken;
    }
}
