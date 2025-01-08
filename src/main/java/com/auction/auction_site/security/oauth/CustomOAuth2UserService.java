package com.auction.auction_site.security.oauth;

import com.auction.auction_site.dto.MemberDto;
import com.auction.auction_site.dto.oauth.GoogleResponse;
import com.auction.auction_site.dto.oauth.KakaoResponse;
import com.auction.auction_site.dto.oauth.NaverResponse;
import com.auction.auction_site.dto.oauth.OAuth2Response;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 소셜 로그인 요청을 처리하고 사용자 정보를 조회/저장하며 CustomOAuth2User를 반환
 *
 * <동작 흐름>
 * 1. 사용자가 소셜 로그인 요청
 * 2. 스프링 시큐리티가 소셜 로그인 provider에 인증 요청을 보냄
 * 3. 소셜 로그인 provider가 사용자 정보를 반환
 * 4. 반환된 정보를 CustomOAuth2UserService가 처리하며 사용자 정보를 데이터베이스에 저장/업데이트
 * 5. OAuth2User 객체를 반환하여 인증 완료
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    /**
     * 네이버나 구글의 사용자 정보 데이터를 인자로 받아옴
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User: {}", oAuth2User.getAttributes());

        String registrationId= userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        // 네이버, 구글, 카카오에서 보내는 인증 데이터 규격이 다르므로 각각의 응답을 처리하는 DTO 생성
        switch (registrationId) {
            case "naver":
                oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
                break;
            case "google":
                oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
                break;
            case "Kakao":
                oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
                break;
            default:
                    return null;
        }

//        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        Member findMember = memberRepository.findByLoginId(oAuth2Response.getEmail());

        MemberDto memberDto = null;

        // 유저 정보가 없으면 저장
        if(findMember == null) {
            Member member = Member.builder()
                    .loginId(oAuth2Response.getEmail())
                    .password(oAuth2Response.getProviderId()) // 제공자 내부에서 사용자를 식별하는 고유 ID로 비밀번호로 처리
                    .name(oAuth2Response.getName())
                    .nickname(oAuth2Response.getNickname())
                    .registerDate(LocalDate.now())
                    .build();

            memberRepository.save(member);

            memberDto = MemberDto.fromMember(member);

        } else { // 유저 정보가 있으면 업데이트
            findMember.setLoginId(oAuth2Response.getEmail());
            findMember.setName(oAuth2Response.getName());

            memberRepository.save(findMember);

            memberDto = MemberDto.fromMember(findMember);
        }

        return new CustomOAuth2User(memberDto);
    }
}