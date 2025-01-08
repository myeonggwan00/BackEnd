package com.auction.auction_site.dto.oauth;

public interface OAuth2Response {
    // 제공자(구글, 네이버)
    String getProvider();

    // 제공자에서 발급해주는 사용자를 식별하기 위한 고유 ID
    String getProviderId();

    // 사용자 이메일
    String getEmail();

    // 사용자명
    String getName();

    // 사용자 닉네임
    String getNickname();
}
