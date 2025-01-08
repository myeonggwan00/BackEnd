package com.auction.auction_site.security.spring_security;

import com.auction.auction_site.dto.LoginMemberDto;
import com.auction.auction_site.entity.RefreshToken;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static com.auction.auction_site.utils.Utility.*;
import static com.auction.auction_site.utils.Utility.createErrorResponse;
import static com.auction.auction_site.utils.ConstantConfig.*;

/**
 * 폼 로그인 방식이 아닌 JSON 방식으로 데이터를 주고받으며 로그인 처리를 하기 위해 커스텀 필터 생성
 */
public class CustomJsonLoginFilter extends AbstractAuthenticationProcessingFilter {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // 해당 필터가 적용되는 요청
    private static final String DEFAULT_LOGIN_REQUEST_URL = "/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";

    // 요청 매핑 조건
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);

    private final ObjectMapper objectMapper;

    public CustomJsonLoginFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper,
                                 JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        this.setAuthenticationManager(authenticationManager);
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // 로그인 요청이 들어올 때 실행되는 메서드
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 요청 Content-Type 확인
        if (!CONTENT_TYPE.equalsIgnoreCase(request.getContentType())) {
            throw new AuthenticationServiceException("Unsupported Content-Type: " + request.getContentType());
        }

        // JSON 데이터를 LoginUserDto 객체로 변환
        LoginMemberDto loginMemberDto = objectMapper.readValue(request.getInputStream(), LoginMemberDto.class);

        // 유효성 검사
        if (!StringUtils.hasText(loginMemberDto.getLoginId()) || !StringUtils.hasText(loginMemberDto.getPassword())) {
            throw new AuthenticationServiceException("Username or Password is missing");
        }

        // Authentication 생성
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(loginMemberDto.getLoginId(), loginMemberDto.getPassword());

        // 인증 요청 객체에 추가 정보 설정??
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException {
        // username 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String loginId = userDetails.getUsername();

        // role 정보 가져오기
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // JWT 토큰(access, refresh) 생성
        String accessToken = jwtUtil.createJwt("access", loginId, role, ACCESS_EXPIRED_MS);
        String refreshToken = jwtUtil.createJwt("refresh", loginId, role, REFRESH_EXPIRED_MS);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .loginId(loginId)
                .refreshToken(refreshToken)
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRED_MS))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("loginId", loginId);
        hashMap.put("role", role);

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(createCookie(refreshToken));
        createSuccessResponse(response, "success", "로그인 성공", hashMap);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        createErrorResponse(response, "fail", "UNAUTHORIZED", "로그인 실패");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refresh", value);

        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
