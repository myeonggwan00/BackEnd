package com.auction.auction_site.repository;

import com.auction.auction_site.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Boolean existsByRefreshToken(String refreshToken);

    @Transactional
    void deleteByRefreshToken(String refreshToken);

    void deleteByLoginId(String loginId);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiration < :now")
    void deleteExpiredRefresh(Date now);
}
