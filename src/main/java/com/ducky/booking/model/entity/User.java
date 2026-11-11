package com.ducky.booking.model.entity;

public final class User extends Account {
    public User(long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "USER";
    }
}
