package com.ducky.booking.model.entity;

// Đại diện thông tin chung của một tài khoản trong hệ thống
public class Account {

    private long id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;

    public Account() {
        // Constructor rỗng để tạo đối tượng trước rồi gán dữ liệu sau
    }

    public Account(
            long id,
            String fullName,
            String email,
            String passwordHash,
            String role
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}