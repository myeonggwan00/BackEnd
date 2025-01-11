package com.auction.auction_site.security.oauth;

import com.auction.auction_site.dto.member.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * OAuth2User 인터페이스를 구현한 클래스며 소셜 로그인 사용자 정보를 담는 클래스
 *
 * <동작 흐름>
 * 1. 소셜 로그인 성공시 OAuth2 Provider가 사용자 정보를 반환
 * 2. 반환된 정보를 CustomOAuth2User 객체에 저장
 * 3. CustomOAuth2User는 스프링 시큐리티의 인증 객체로 사용되어 사용자 인증 및 권한 처리를 담당
 */
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final MemberDto memberDto;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
    public String getName() {
        return memberDto.getLoginId();
    }

    public String getLoginId() {
        return memberDto.getLoginId();
    }
}
