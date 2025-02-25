package com.auction.auction_site.security.spring_security;

import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.exception.EntityNotFound;
import com.auction.auction_site.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 시큐리티의 UserDetailsService 인터페이스를 구현한 클래스
 * 데이터베이스에서 사용자 정보를 조회하고 CustomUserDetails 객체를 생성하여 변환
 * *
 * <동작 흐름>
 * 1. UsernamePasswordAuthenticationFilter가 로그인 요청을 처리
 * 2. loadUserByUsername() 메서드를 호출하여 사용자 조회
 * 3. 조회된 사용자 정보를 CustomUserDetails에 담아서 반환
 * 4. 스프링 시큐리티가 이를 검증하여 인증 성공/실패를 결정
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new EntityNotFound("가입되지 않은 회웝입니다."));

        return new CustomUserDetails(MemberDto.fromMember(member));
    }
}
