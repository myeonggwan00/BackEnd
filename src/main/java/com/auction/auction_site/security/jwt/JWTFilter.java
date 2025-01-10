package com.auction.auction_site.security.jwt;

import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.entity.Role;
import com.auction.auction_site.security.oauth.CustomOAuth2User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.auction.auction_site.utils.Utility.*;

/**
 * JWT 토큰을 검증하는 필터
 */
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        try {
            jwtUtil.isExpired(token); // 토큰이 만료되었는지 확인
        } catch (ExpiredJwtException e) {
            createErrorResponse(response, "fail", "EXPIRED", "토큰이 만료되었습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            // doFilter 메서드를 통해 다음 필터를 호출하면 안된다.

            return;
        }

        String category = jwtUtil.getCategory(token); // 토큰에서 category 값 가져오기

        if (!category.equals("access")) { // category 값이 access가 아닌 경우
            createErrorResponse(response, "fail", "UNAUTHORIZED", "유효하지 않은 토큰입니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰에서 username, role 획득
        String loginId = jwtUtil.getLoginId(token);
        String role = jwtUtil.getRole(token);

        MemberDto memberDto = MemberDto.builder()
                .loginId(loginId)
                .role(Role.valueOf(role))
                .build();

        /**
         * UserDetails에 회원 정보 객체 담기
         * 소셜 로그인과 일반 로그인 모두 JWT 토큰에 userId, role 값을 담는다.
         * 따라서 일반 로그인의 경우에도 CustomUserDetails 객체에 회원 정보를 담지 않고 CustomOAuth2User 객체에 정보를 담아도 된다.
         * 만약 서로 다른 값을 JWT 토큰에 담을시 일반 로그인의 경우 CustomUserDetails, 소셜 로그인의 경우 CustomOAuth2User에 회원 정보를 담아야 한다.
         */
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(memberDto);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());

        // 세션(일시적)에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 다음 필터 호출
        filterChain.doFilter(request, response);
    }
}