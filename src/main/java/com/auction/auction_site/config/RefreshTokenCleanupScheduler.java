package com.auction.auction_site.config;

import com.auction.auction_site.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?") // 스케줄러 작업 정의 - 매일 해당 시간에 해당 메서드 실행
    public void deleteExpiredTokens() {
        Date now = new Date();
        refreshTokenRepository.deleteExpiredRefresh(now);

        System.out.println("만료된 Refresh 토큰 삭제 완료: " + now);
    }
}
