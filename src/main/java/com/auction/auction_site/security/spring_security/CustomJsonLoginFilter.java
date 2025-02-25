package com.auction.auction_site.security.spring_security;

import com.auction.auction_site.dto.member.LoginMemberDto;
import com.auction.auction_site.dto.member.MemberResponseDto;
import com.auction.auction_site.entity.RefreshToken;
import com.auction.auction_site.exception.EntityNotFound;
import com.auction.auction_site.repository.MemberRepository;
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

import static com.auction.auction_site.utils.Utility.*;
import static com.auction.auction_site.config.ConstantConfig.*;

/**
 * 폼 로그인 방식이 아닌 JSON 방식으로 데이터를 주고받으며 로그인 처리를 하기 위해 커스텀 필터 생성
 */
public class CustomJsonLoginFilter extends AbstractAuthenticationProcessingFilter {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    // 로그인 필터가 적용되는 요청 설정
    private static final String DEFAULT_LOGIN_REQUEST_URL = "/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";

    /**
     * 요청 매핑 조건 설정
     * AntPathRequestMatcher는 특정 URL과 HTTP 메서드를 매칭하는 역할
     * AntRequestMatcher는 스프링 시큐리티 필터에서 특정 URL 패턴을 필터링할 때 필수
     * 즉, AntPathRequestMatcher를 사용하면 특정 URL 패턴에 대해서만 필터가 동작하게 할 수 있다.
     */
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);

    public CustomJsonLoginFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper,
                                 JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository, MemberRepository memberRepository) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        this.setAuthenticationManager(authenticationManager); // 해당 필터에서 AuthenticationManager를 사용하도록 설정
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.memberRepository = memberRepository;
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

        // UsernamePasswordAuthenticationToken 생성
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(loginMemberDto.getLoginId(), loginMemberDto.getPassword());

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal(); // 회원 정보
        String loginId = userDetails.getUsername(); // 회원 로그인 아이디
        String role = authentication.getAuthorities().iterator().next().getAuthority(); // 회원 권한

        MemberResponseDto memberResponseDto = MemberResponseDto.from(
                memberRepository.findByLoginId(loginId).orElseThrow(() -> new EntityNotFound("가입되지 않은 회웝입니다.")));

        // JWT access 토큰 생성
        String accessToken = jwtUtil.createJwt("access", loginId, role, ACCESS_EXPIRED_MS);

        // JWT refresh 토큰 생성
        RefreshToken refreshToken = refreshTokenRepository
                .findByLoginIdAndExpirationAfter(loginId, new Date())
                .orElseGet(() -> { // 만료되지 않은 refresh 토큰이 없으면 새로 생성
                    String newRefreshToken = jwtUtil.createJwt("refresh", loginId, role, REFRESH_EXPIRED_MS);

                    RefreshToken refreshTokenEntity = RefreshToken.builder()
                            .loginId(loginId)
                            .refreshToken(newRefreshToken)
                            .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRED_MS))
                            .build();

                    refreshTokenRepository.save(refreshTokenEntity);

                    return refreshTokenEntity;
                });

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(createCookie(refreshToken.getRefreshToken()));
        sendSuccessJsonResponse(response, "로그인 성공", memberResponseDto);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        sendErrorJsonResponse(response, "UNAUTHORIZED", "로그인 실패");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * 쿠키 생성 메서드
     */
    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("refresh", value);

        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);

        return cookie;
    }
}