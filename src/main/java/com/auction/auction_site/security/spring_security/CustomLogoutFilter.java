package com.auction.auction_site.security.spring_security;

import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static com.auction.auction_site.utils.Utility.*;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();

        // 요청 경로 확인
        if(!requestURI.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();

        // HTTP 메서드 확인
        if(!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // refresh 토큰 가져오기
        String refresh = null;

        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            sendErrorJsonResponse(response, "UNAUTHORIZED", "로그아웃에 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        for (Cookie cookie : cookies) { // cookies가 null일 때 반복을 수행할 수 없으므로 NPE 발생
            if(cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        // refresh 토큰 널 체크
        if (refresh == null) {
            sendErrorJsonResponse(response, "UNAUTHORIZED", "로그아웃에 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refresh); // refresh 토큰 만료되었느지 확인
        } catch(ExpiredJwtException e) {
            sendErrorJsonResponse(response, "EXPIRED", "토큰이 만료되어 이미 로그아웃된 상태입니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            refreshTokenRepository.deleteByRefreshToken(refresh);
            return;
        }

        String category = jwtUtil.getCategory(refresh);

        // 발급된 토큰이 유형 확인
        if(!category.equals("refresh")) {
            sendErrorJsonResponse(response, "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Boolean isExist = refreshTokenRepository.existsByRefreshToken(refresh);

        // refresh 토큰이 데이터베이스에 저장되어 있는지 확인
        if(!isExist) {
            sendErrorJsonResponse(response, "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 로그아웃 진행 - 데이터베이스에서 Refresh 토큰 삭제
        refreshTokenRepository.deleteByRefreshToken(refresh);

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        sendSuccessJsonResponse(response, "로그아웃 완료", null);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
