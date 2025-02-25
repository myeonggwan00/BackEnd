package com.auction.auction_site.security.spring_security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.auction.auction_site.utils.Utility.sendErrorJsonResponse;

/**
 * 스프링 시큐리티에서 인증되지 않은 사용자가 보호된 자원에 접근하려고 할 때 처리
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        sendErrorJsonResponse(response, "UNAUTHORIZED", "인증되지 않은 사용자입니다.");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
