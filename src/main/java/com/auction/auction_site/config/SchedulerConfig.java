package com.auction.auction_site.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Redis 사용시 TTL 설정을 통해 생명주기가 끝난 토큰은 자동으로 삭제해준다.
 * MySQL 같은 RDB 경우 생명주기가 끝난 토큰을 주기적으로 스케줄 작업을 통해 삭제해줘야 한다.
 * 우선 MySQL 적용 후 시간이 되면 Redis 적용 예정
 */
@Configuration
@EnableScheduling // 스켈줄링 기능을 활성화
public class SchedulerConfig {
}
