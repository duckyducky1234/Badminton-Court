package com.ducky.booking.model.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

final class PasswordHash {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    // Use the same amount of work when the email does not exist.
    static final String DUMMY_HASH = encode(new byte[SALT_BYTES], new byte[KEY_BITS / 8]);

    private PasswordHash() {}

    static String create(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return encode(salt, derive(password, salt, ITERATIONS));
    }

    static boolean matches(String password, String storedHash) {
        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != 4 || !ALGORITHM.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = DECODER.decode(parts[2]);
            byte[] expected = DECODER.decode(parts[3]);
            if (iterations < 1 || iterations > 2_000_000
                    || salt.length != SALT_BYTES || expected.length != KEY_BITS / 8) {
                return false;
            }
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static String encode(byte[] salt, byte[] key) {
        return String.join("$", ALGORITHM, Integer.toString(ITERATIONS),
                ENCODER.encodeToString(salt), ENCODER.encodeToString(key));
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        char[] chars = password.toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(chars, salt, iterations, KEY_BITS);
        Arrays.fill(chars, '\0');
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        } finally {
            keySpec.clearPassword();
        }
    }
}
