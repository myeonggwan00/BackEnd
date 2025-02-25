package com.auction.auction_site.config;

/**
 * 상수 관리 클래스
 */
public class ConstantConfig {
    public static final Long ACCESS_EXPIRED_MS = 60 * 60 * 1000L; // 한시간
    public static final Long REFRESH_EXPIRED_MS = 24 * 60 * 60 * 100L;
    public static final int COOKIE_MAX_AGE = 24 * 60 * 60;
}