package com.auction.auction_site.service;

import com.auction.auction_site.entity.RefreshToken;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static com.auction.auction_site.config.ConstantConfig.*;
import static com.auction.auction_site.utils.Utility.sendErrorJsonResponse;
import static com.auction.auction_site.utils.Utility.sendSuccessJsonResponse;

/**
 * 토큰 재발급과 관련된 비즈니스 로직을 처리하는 서비스
 * *
 * Access 토큰은 프론트 측에서 관리, Refresh 토큰은 백엔드 측에서 관리
 * 서버측에서 JWTFilter를 통해 Access 토큰 만료로 인한 특정 상태 코드를 응답하면?
 * 프론트측에서 예외 핸들러로 Access 토큰 재발행을 위해서 Refresh 토큰과 함께 해당 컨트롤러 전달
 * 서버측은 전달받은 Refresh 토큰을 통해 새로운 Access 토큰과 Refresh 토큰을 응답
 * Refresh 토큰도 새로 만든 이유? 보안을 위해 Access 토큰 뿐 아니라 Refresh 토큰도 함께 갱신(Refresh Rotate)
 */
@Service
@RequiredArgsConstructor
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<String> validateAndGetRefreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = null; // 초기화

        Cookie[] cookies = request.getCookies(); // 요청에서 쿠키 가져오기

        if(cookies == null) { // 쿠키가 없는 경우 예외 발생
            sendErrorJsonResponse(response, "UNAUTHORIZED", "토큰 재발급시 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Optional.empty();
        }

        for (Cookie cookie : cookies) { // 쿠키가 있는 경우
            if (cookie.getName().equals("Authorization")) { // 쿠키에서 Refresh 토큰 가져오기
                refreshToken = cookie.getValue();
            }
        }

        if(refreshToken == null) { // 토큰의 널 체크
            sendErrorJsonResponse(response, "UNAUTHORIZED", "토큰 재발급시 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Optional.empty();
        }

        try { // 토큰의 만료여부 확인
            jwtUtil.isExpired(refreshToken);
        } catch(ExpiredJwtException e) {
            sendErrorJsonResponse(response, "EXPIRED", "토큰이 만료되었습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Optional.empty();
        }

        String category = jwtUtil.getCategory(refreshToken); // 토큰의 category 값 가져오기

        if(!category.equals("refresh")) { // JWT 토큰이 Refresh 토큰인지 확인
            sendErrorJsonResponse(response, "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Optional.empty();
        }

        return Optional.of(refreshToken);
    }

    public void reissueToken(HttpServletResponse response, String refreshToken) throws IOException {

        // refresh 토큰에서 loginId, role 가져오기
        String loginId = jwtUtil.getLoginId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 토큰(access, refresh) 생성
        String newAccessToken = jwtUtil.createJwt("access", loginId, role, ACCESS_EXPIRED_MS);
        String newRefreshToken = jwtUtil.createJwt("refresh", loginId, role, REFRESH_EXPIRED_MS);

        refreshTokenRepository.deleteByRefreshToken(refreshToken); // 이전의 refresh 토큰 삭제

        RefreshToken refreshTokenEntity = RefreshToken.builder() // refresh 토큰 새로 생성
                .loginId(loginId)
                .refreshToken(newRefreshToken)
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRED_MS))
                .build();

        refreshTokenRepository.save(refreshTokenEntity); // 새로 생성한 refresh 토큰 저장

        setTokenResponseHeaders(response, newAccessToken, newRefreshToken); // 토큰을 가지고 응답 생성
    }

    private void setTokenResponseHeaders(HttpServletResponse response, String newAccessToken, String newRefreshToken) throws IOException {
        // access 토큰은 헤더에, refresh 토큰은 쿠키에 담아서 응답
        response.setStatus(HttpServletResponse.SC_OK);
        response.addCookie(createCookie(newRefreshToken));
        response.setHeader("Authorization", "Bearer " + newAccessToken);

        sendSuccessJsonResponse(response, "토큰 재발급 완료", null);
    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refresh", value);

        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true); // 자바스크립트 공격 방지

        return cookie;
    }
}
