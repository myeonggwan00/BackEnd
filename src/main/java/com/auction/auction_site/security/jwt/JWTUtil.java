package com.auction.auction_site.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 유틸리티 클래스
 */
@Component
public class JWTUtil {
    private final SecretKey secretKey; // JWT 서명에 사용할 비밀키를 저장하는 변수

    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
        secretKey = new SecretKeySpec( // SecretKeySpec 객체는 비밀키를 사용하여 JWT 서명을 위한 비밀 키 생성
                secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build() // JWT 토큰를 파싱하면서 서명 검증을 진행
                .parseSignedClaims(token) // JWT 토큰을 파싱하면서 서명된 클레임을 추출
                .getPayload().get("category", String.class); // 파싱된 JWT 페이로드에서 해당 클레임 가져옴
    }

    public String getLoginId(String token) {
        return Jwts.parser().verifyWith(secretKey).build() // JWT 토큰를 파싱하면서 서명 검증을 진행
                .parseSignedClaims(token) // JWT 토큰을 파싱하면서 서명된 클레임을 추출
                .getPayload().get("loginId", String.class); // 파싱된 JWT 페이로드에서 해당 클레임 가져옴
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload().get("role", String.class);
    }

    public void isExpired(String token) {
        Jwts.parser().verifyWith(secretKey).build()
                // JWT 토큰을 파싱하면서 서명된 클레임을 추출(만료 시간을 내부적을 확인하여 만료된 토큰에 대해 자동으로 ExpiredJwtException을 던짐)
                .parseSignedClaims(token)
                .getPayload();
    }

    // 새로운 JWT 토큰을 생성하는 createJwt
    public String createJwt(String category, String loginId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("loginId", loginId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // 현재 시각으로 JWT 발행일자를 설정
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // JWT 만료일자 설정
                .signWith(secretKey) // 서명을 할 때 사용할 비밀 키 설정
                .compact(); // JWT 토큰을 생성하여 반환
    }
}
