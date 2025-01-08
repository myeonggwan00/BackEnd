package com.auction.auction_site.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Builder
@Getter @Setter
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId; // 하나의 사용자가 여러 토큰을 발급 받을 수 있음

    private String refreshToken;

    private Date expiration;

    public RefreshToken() {

    }
}
