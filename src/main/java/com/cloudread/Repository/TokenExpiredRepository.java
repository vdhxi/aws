package com.cloudread.Repository;

import com.cloudread.Entity.TokenExpired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenExpiredRepository extends JpaRepository<TokenExpired, String> {
}
