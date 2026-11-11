package com.ducky.booking.model.service;

import com.ducky.booking.config.DatabaseConnection;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.dao.AccountDAO;
import com.ducky.booking.model.entity.Account;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AuthService {
    private static final Pattern EMAIL = Pattern.compile(
            "[^\\s@\\p{Cntrl}]+@[^\\s@\\p{Cntrl}]+\\.[^\\s@\\p{Cntrl}]+");
    private static final int MAX_PASSWORD_LENGTH = 128;

    private final AccountDAO accounts;

    public AuthService() {
        this(new DatabaseConnection());
    }

    public AuthService(DatabaseConnection database) {
        this.accounts = new AccountDAO(database);
    }

    public Account register(String name, String email, String password) throws SQLException, BusinessException {
        return createAccount(name, email, password, "USER");
    }

    // The command-line bootstrap is the only caller that can create an administrator.
    public Account createAdmin(String name, String email, String password) throws SQLException, BusinessException {
        return createAccount(name, email, password, "ADMIN");
    }

    public Account login(String email, String password) throws SQLException, BusinessException {
        String normalizedEmail = normalizeEmail(email);
        if (!isValidEmail(normalizedEmail) || password == null || password.length() > MAX_PASSWORD_LENGTH) {
            throw invalidLogin();
        }
        AccountDAO.Credentials credentials = accounts.findCredentials(normalizedEmail);
        String hash = credentials == null ? PasswordHash.DUMMY_HASH : credentials.passwordHash();
        if (!PasswordHash.matches(password, hash) || credentials == null) {
            throw invalidLogin();
        }
        return credentials.account();
    }

    private Account createAccount(String name, String email, String password, String role)
            throws SQLException, BusinessException {
        String normalizedName = name == null ? "" : name.strip();
        String normalizedEmail = normalizeEmail(email);
        if (normalizedName.isEmpty() || normalizedName.length() > 100
                || normalizedName.chars().anyMatch(Character::isISOControl)) {
            throw new BusinessException("Họ tên phải có từ 1 đến 100 ký tự và không chứa ký tự điều khiển.");
        }
        if (!isValidEmail(normalizedEmail)) {
            throw new BusinessException("Địa chỉ email không hợp lệ.");
        }
        if (password == null || password.length() < 8 || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BusinessException("Mật khẩu phải có từ 8 đến 128 ký tự.");
        }

        Account account = accounts.insert(normalizedName, normalizedEmail, PasswordHash.create(password), role);
        if (account == null) {
            throw new BusinessException("Email này đã được đăng ký.");
        }
        return account;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }

    private boolean isValidEmail(String email) {
        return email.length() <= 254 && EMAIL.matcher(email).matches();
    }

    private BusinessException invalidLogin() {
        return new BusinessException("Email hoặc mật khẩu không đúng.");
    }
}
