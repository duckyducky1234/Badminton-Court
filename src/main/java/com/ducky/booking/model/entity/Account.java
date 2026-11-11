package com.ducky.booking.model.entity;

public abstract sealed class Account permits User, Admin {
    private final long id;
    private final String name;
    private final String email;

    protected Account(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public abstract String getRole();

    public final boolean isAdmin() {
        return this instanceof Admin;
    }
}
