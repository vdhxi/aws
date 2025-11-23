package com.cloudread.Utils;

import com.cloudread.Entity.Users;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class RandomCode {
    String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    SecureRandom random;

    public String generateCodeByUserId(Users user, int length) {
        try {
            String seed = user.getId() + System.nanoTime();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));

            StringBuilder code = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int index = (hash[i] & 0xff) % CHAR_POOL.length();
                code.append(CHAR_POOL.charAt(index));
            }
            return code.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available");
        }
    }

    public String generateSecureCode(Users user, int length) {

        random.setSeed((user.getId() + System.nanoTime()).getBytes());

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            code.append(CHAR_POOL.charAt(index));
        }
        return code.toString();
    }

    public static String generateOtp6Digits() {
        int otp = (int) (Math.random() * 1000000);
        return String.format("%06d", otp);
    }
}
