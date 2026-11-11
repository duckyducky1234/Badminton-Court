package com.ducky.booking.model.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.dao.AccountDAO;
import com.ducky.booking.model.entity.Account;

public class AuthService {

    private AccountDAO accountDAO;

    public AuthService() {
        accountDAO = new AccountDAO();
    }

    // Kiểm tra đăng nhập
    public Account login(String email, String password) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập email.");
        }

        if (password == null || password.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mật khẩu.");
        }

        email = email.trim().toLowerCase();
        Account account = accountDAO.findByEmail(email);

        if (account == null) {
            throw new BusinessException("Email hoặc mật khẩu không đúng.");
        }

        String passwordHash = hashPassword(password);

        if (!passwordHash.equals(account.getPasswordHash())) {
            throw new BusinessException("Email hoặc mật khẩu không đúng.");
        }

        return account;
    }

    // Đăng ký tài khoản khách hàng
    public void register(String fullName, String email, String password, String confirmPassword)
            throws SQLException {

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập họ và tên.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập email.");
        }

        if (!email.contains("@")) {
            throw new BusinessException("Email không hợp lệ.");
        }

        if (password == null || password.length() < 6) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        if (!password.equals(confirmPassword)) {
            throw new BusinessException("Mật khẩu xác nhận không khớp.");
        }

        fullName = fullName.trim();
        email = email.trim().toLowerCase();

        if (accountDAO.existsByEmail(email)) {
            throw new BusinessException("Email này đã được sử dụng.");
        }

        Account account = new Account(
                0, fullName, email, hashPassword(password), "USER"
        );

        accountDAO.create(account);
    }

    // Băm mật khẩu trước khi lưu vào database
    private String hashPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] bytes = messageDigest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hash = new StringBuilder();

            for (byte item : bytes) {
                hash.append(String.format("%02x", item));
            }

            return hash.toString();

        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("Không thể xử lý mật khẩu.", exception);
        }
    }
}