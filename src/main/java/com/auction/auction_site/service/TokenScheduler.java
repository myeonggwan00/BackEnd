package com.auction.auction_site.service;

import com.auction.auction_site.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 만료된 토큰 처리
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 스케줄러 작업 정의 - 매일 해당 시간에 해당 메서드 실행
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredRefresh(new Date());
    }
}
