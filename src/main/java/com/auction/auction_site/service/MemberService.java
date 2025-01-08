package com.auction.auction_site.service;

import com.auction.auction_site.dto.MemberDto;
import com.auction.auction_site.dto.UpdateMemberDto;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.repository.MemberRepository;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean checkLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public boolean checkNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public MemberDto registerProcess(MemberDto memberDto) {
        if(memberRepository.existsByLoginId(memberDto.getLoginId())) { // 중복 검사
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        Member member = Member.builder()
                .loginId(memberDto.getLoginId())
                .password(bCryptPasswordEncoder.encode(memberDto.getPassword()))
                .name(memberDto.getName())
                .nickname(memberDto.getNickname())
                .registerDate(LocalDate.now())
                .build();

        memberRepository.save(member);

        return memberDto.fromMember(member);
    }

    @Transactional
    public MemberDto updateMember(UpdateMemberDto updateMemberDto, String token) {
//        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFound("User with id " + id + " not found"));

        String loginId = jwtUtil.getLoginId(token);

        Member member = memberRepository.findByLoginId(loginId);

        member.setNickname(updateMemberDto.getNickname());
        member.setPassword(bCryptPasswordEncoder.encode(updateMemberDto.getPassword()));

        return MemberDto.fromMember(member);
    }

    @Transactional
    public void deleteProcess(String token) {
        String loginId = jwtUtil.getLoginId(token);

        Member findMember = memberRepository.findByLoginId(loginId);

        memberRepository.delete(findMember);
        refreshTokenRepository.deleteByLoginId(loginId);
    }
}
