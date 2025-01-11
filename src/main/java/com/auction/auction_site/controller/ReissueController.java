package com.auction.auction_site.controller;

import com.auction.auction_site.utils.Utility;
import com.auction.auction_site.entity.RefreshToken;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import static com.auction.auction_site.utils.ConstantConfig.*;
import static com.auction.auction_site.utils.Utility.*;

/**
 * ReissueController
 * - 해당 컨트롤러는 전달받은 Refresh 토큰을 받아 새로운 Access 토큰을 응답하는 컨트롤러
 *
 * - 도입이유
 * 서버측에서 JWTFilter를 통해 Access 토큰 만료로 인한 특정 상태 코드를 응답하면?
 * 프론트측에서 예외 핸들러로Access 토큰 재발행을 위한 Refresh 토큰을 전달
 * 서버측은 전달받은 Refresh 토큰을 받아 새로운 Access 토큰을 응답
 *
 * + 추가
 * 보안을 위해 Access 토큰 뿐 아니라 Refresh 토큰도 함께 갱신(Refresh Rotate)
 * 단, Rotate 되기 전의 토큰을 탈취 했을 경우 해당 토큰으로도 인증되므로 서버측에서 발급했던 Refresh 추가 처리 작업 필요
 */
@RestController
@RequiredArgsConstructor
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/reissue")
    public void reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            sendErrorJsonResponse(response, "UNAUTHORIZED", "토큰 재발급시 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }


        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("Authorization")) {
                refreshToken = cookie.getValue();
            }
        }

        if(refreshToken == null) { // Refresh 토큰의 널 체크
            sendErrorJsonResponse(response, "UNAUTHORIZED", "토큰 재발급시 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        try { // Refresh 토큰의 만료여부 확인
            jwtUtil.isExpired(refreshToken);
        } catch(ExpiredJwtException e) {
            sendErrorJsonResponse(response, "EXPIRED", "토큰이 만료되었습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        String category = jwtUtil.getCategory(refreshToken); // Refresh 토큰의 category 값 가져오기

        if(!category.equals("refresh")) { // JWT 토큰이 Refresh 토큰인지 확인
            sendErrorJsonResponse(response, "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        // Refresh 토큰에서 username, role 가져오기
        String loginId = jwtUtil.getLoginId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 토큰(access, refresh) 생성
        String newAccessToken = jwtUtil.createJwt("access", loginId, role, ACCESS_EXPIRED_MS);
        String newRefreshToken = jwtUtil.createJwt("refresh", loginId, role, REFRESH_EXPIRED_MS);

        // 이전의 refresh 토큰 삭제
        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .loginId(loginId)
                .refreshToken(newRefreshToken)
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRED_MS))
                .build();

        // 생성한 refresh 토큰 저장
        refreshTokenRepository.save(refreshTokenEntity);

        // access 토큰은 헤더에, refresh 토큰은 쿠키에 담아서 응답
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie(newRefreshToken));
        sendSuccessJsonResponse(response, "토큰 재발급 완료", null);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refresh", value);

        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true); // 자바스크립트 공격 방지

        return cookie;
    }
}