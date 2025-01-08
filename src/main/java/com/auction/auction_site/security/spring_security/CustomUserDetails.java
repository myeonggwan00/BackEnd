package com.auction.auction_site.security.spring_security;

import com.auction.auction_site.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 사용자 인증 및 권한에 필요한 username, password, role 정보를 저장하는 클래스
 *
 * <동작 흐름>
 * 1. 로그인 요청 시, CustomUserDetailsService를 통해 데이터베이스에서 사용자 정보를 조회
 * 2. 조회된 정보를 CustomUserDetails에 담음
 * 3. 스프링 시큐리티 인증 필터가 CustomUserDetails를 검증하여 Authentication 객체로 변환
 * 4. 최종적으로 SecurityContext에 인증 정보를 저장
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final MemberDto memberDto;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 사용자의 특정한 권한 반환
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberDto.getRole().toString();
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {
        return memberDto.getPassword();
    }

    @Override
    public String getUsername() {
        return memberDto.getLoginId();
    } // username이 아닌 userId로 인증 처리
}
