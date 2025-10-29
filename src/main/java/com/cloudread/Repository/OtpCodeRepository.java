package com.cloudread.Repository;

import com.cloudread.Entity.OtpCode;
import com.cloudread.Enum.TokenScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Integer> {
    @Transactional
    @Modifying
    @Query("DELETE FROM OtpCode otp WHERE otp.expiresAt < :now")
    void deleteOtpCode(@Param("now") LocalDateTime now);

    OtpCode findByEmailAndOtp(String email, String otp);

    OtpCode findByEmailAndScope(String email, TokenScope scope);
}
