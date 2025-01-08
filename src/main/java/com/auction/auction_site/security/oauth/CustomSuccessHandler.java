package com.auction.auction_site.security.oauth;

import com.auction.auction_site.entity.RefreshToken;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.auction.auction_site.utils.ConstantConfig.COOKIE_MAX_AGE;
import static com.auction.auction_site.utils.ConstantConfig.REFRESH_EXPIRED_MS;

/**
 * OAuth2 로그인 인증 성공시 실행되는 핸들러
 */
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String loginId = customUserDetails.getLoginId();

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String refreshToken = jwtUtil.createJwt("refresh", loginId, role, REFRESH_EXPIRED_MS);

        response.addCookie(createCookie("Authorization", refreshToken));

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .loginId(loginId)
                .refreshToken(refreshToken)
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRED_MS))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

//        response.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_MAX_AGE);
//        cookie.setSecure(true); // HTTPS 통신을 진행할 경우 추가
        cookie.setPath("/"); // 쿠키가 적용될 범위
        cookie.setHttpOnly(true); // 자바스크립 공격 방지를 위해 추가

        return cookie;
    }
}
