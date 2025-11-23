package com.cloudread.Service.Impl;

import com.cloudread.Service.CleanerOtpService;
import com.cloudread.Repository.OtpCodeRepository;
import com.cloudread.Repository.TokenValidRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CleanerOtpServiceImpl implements CleanerOtpService {
    OtpCodeRepository otpCodeRepository;
    TokenValidRepository tokenValidRepository;

    // Delete otp every 5 mins
    @Scheduled(fixedRate = 5 * 60 * 1000) // Milliseconds
    public void cleanOtpCode() {
        LocalDateTime now = LocalDateTime.now();
        otpCodeRepository.deleteOtpCode(now);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanExpiredToken() {
        LocalDateTime now = LocalDateTime.now();
        tokenValidRepository.deleteExpiredTokens(now);
        tokenValidRepository.deleteExpiredTokens(true);
    }
}
