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

/**
 * 로그아웃 필터
 * access 토큰은 보통 만료기간을 짧게 가지고 서버측에서 access 토큰을 관리하지 않고 프론트 측에서 관리
 * 로그아웃의 경우 인증된 회원만 진행하므로 access 토큰을 넘겨줘야 하며 추가로 refresh 토큰도 같이 넘겨서 refresh 토큰을 삭제
 * 이렇게 되면 access 토큰이 만료되더라도 토큰을 재발행을 할 수 없음
 */
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestURI = request.getRequestURI(); // URI 정보 가져오기

        // 요청 경로 확인
        if(!requestURI.matches("/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod(); // 메서드 정보 가져오기

        // HTTP 메서드 확인
        if(!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = null; // 초기화

        Cookie[] cookies = request.getCookies(); // 쿠키 가져오기

        if(cookies == null) { // 쿠키가 없는 경우
            sendErrorJsonResponse(response, "UNAUTHORIZED", "로그아웃에 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue(); // 토큰 가져오기
            }
        }

        if (refreshToken == null) { // 토큰 널 체크
            sendErrorJsonResponse(response, "UNAUTHORIZED", "로그아웃에 필요한 인증 정보가 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refreshToken); // 토큰이 만료되었는지 확인
        } catch(ExpiredJwtException e) {
            sendErrorJsonResponse(response, "EXPIRED", "토큰이 만료되어 이미 로그아웃된 상태입니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            refreshTokenRepository.deleteByRefreshToken(refreshToken);
            return;
        }

        String category = jwtUtil.getCategory(refreshToken); // 토큰의 category 값 가져오기

        if(!category.equals("refresh")) { // JWT 토큰이 Refresh 토큰인지 확인
            sendErrorJsonResponse(response, "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // refresh 토큰이 데이터베이스에 저장되어 있는지 확인
        if(!refreshTokenRepository.existsByRefreshToken(refreshToken)) {
            sendErrorJsonResponse(response, "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 로그아웃 진행 - 데이터베이스에서 Refresh 토큰 삭제
        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        sendSuccessJsonResponse(response, "로그아웃 완료", null);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
