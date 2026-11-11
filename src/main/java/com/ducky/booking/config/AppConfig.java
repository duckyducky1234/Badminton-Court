package com.ducky.booking.config;

public final class AppConfig {
    private AppConfig() {
    }

    public static String get(String key) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? null : value.strip();
    }
}
