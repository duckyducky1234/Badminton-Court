package com.ducky.booking.model.entity;

public final class Admin extends Account {
    public Admin(long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
