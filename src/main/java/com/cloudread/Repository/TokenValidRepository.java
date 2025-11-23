package com.cloudread.Repository;

import com.cloudread.Entity.TokenValid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface TokenValidRepository extends JpaRepository<TokenValid, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM TokenValid t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query("DELETE FROM TokenValid t WHERE t.expired = :status")
    void deleteExpiredTokens(@Param("status") boolean status);

    TokenValid findByToken(String token);
}