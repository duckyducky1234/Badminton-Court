package com.ducky.booking.model.service;

import com.ducky.booking.config.AppConfig;
import com.ducky.booking.model.BusinessException;
import com.ducky.booking.model.entity.Booking;
import com.ducky.booking.model.entity.BookingStatus;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class PaymentQrService {
    private final String bankId;
    private final String accountNumber;
    private final String accountName;

    public PaymentQrService() {
        this(AppConfig.get("BOOKING_PAYMENT_BANK_BIN"),
                AppConfig.get("BOOKING_PAYMENT_ACCOUNT_NUMBER"),
                AppConfig.get("BOOKING_PAYMENT_ACCOUNT_NAME"));
    }

    public PaymentQrService(String bankId, String accountNumber, String accountName) {
        this.bankId = bankId;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
    }

    public boolean isConfigured() {
        return bankId != null && bankId.matches("[0-9]{6}")
                && accountNumber != null && accountNumber.matches("[A-Za-z0-9]{1,19}")
                && accountName != null && !accountName.isBlank();
    }

    public String getBankId() {
        return bankId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String transferReference(Booking booking) {
        return "SAN " + booking.getId();
    }

    public byte[] render(Booking booking) throws BusinessException {
        if (!isConfigured()) {
            throw new BusinessException("Thông tin tài khoản nhận chuyển khoản chưa được cấu hình.");
        }
        if (booking.getStatus() != BookingStatus.HELD) {
            throw new BusinessException("Lượt đặt này không còn chờ chuyển khoản.");
        }
        if (booking.getAmount() < 1 || booking.getAmount() >= 500_000_000L) {
            throw new BusinessException("Số tiền nằm ngoài hạn mức chuyển khoản VietQR.");
        }
        return QrImage.png(payload(booking));
    }

    // VietQR NAPAS247: format, initiation method, beneficiary, currency, amount, country, and transfer note.
    private String payload(Booking booking) {
        String beneficiary = field("00", bankId) + field("01", accountNumber);
        String merchantAccount = field("00", "A000000727")
                + field("01", beneficiary)
                + field("02", "QRIBFTTA");
        String payload = field("00", "01")
                + field("01", "12")
                + field("38", merchantAccount)
                + field("53", "704")
                + field("54", Long.toString(booking.getAmount()))
                + field("58", "VN")
                + field("62", field("08", transferReference(booking)))
                + "6304";
        return payload + checksum(payload);
    }

    // All fields used above are ASCII. The length field occupies exactly two digits.
    private String field(String id, String value) {
        return String.format(Locale.ROOT, "%s%02d%s", id, value.length(), value);
    }

    private String checksum(String payload) {
        int crc = 0xFFFF;
        for (byte value : payload.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (value & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                boolean highBitSet = (crc & 0x8000) != 0;
                crc = (crc << 1) & 0xFFFF;
                if (highBitSet) {
                    crc ^= 0x1021;
                }
            }
        }
        return String.format(Locale.ROOT, "%04X", crc);
    }
}
