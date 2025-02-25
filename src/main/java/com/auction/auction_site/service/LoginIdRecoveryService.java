package com.auction.auction_site.service;

import com.auction.auction_site.entity.VerificationToken;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.exception.EntityNotFound;
import com.auction.auction_site.exception.ExpiredTokenException;
import com.auction.auction_site.exception.InvalidTokenException;
import com.auction.auction_site.repository.VerificationTokenRepository;
import com.auction.auction_site.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 로그인 아이디를 찾기 위한 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoginIdRecoveryService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final MemberRepository memberRepository;

    /**
     * 해당 이메일로 가입된 회원이 있는지 확인
     */
    public void validateEmailExists(String email) {
        memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFound("이메일에 해당하는 사용자를 찾을 수 없습니다."));
    }

    /**
     * 로그인 아이디 찾을 때 사용하는 토큰 생성
     * 만약 존재하면? 토큰을 생성하는 것이 아닌 토큰을 갱신
     * 만약 존재하지 않으면? 토큰 생성
     */
    public String createToken(String email) {
        String token = UUID.randomUUID().toString(); // UUID 이용

        // 이메일에 해당하는 토큰이 있는지 확인
        VerificationToken recoveryToken = verificationTokenRepository.findByEmail(email)
                // 토큰 생성
                .orElseGet(() -> verificationTokenRepository.save(
                        VerificationToken.builder()
                                .token(token)
                                .email(email)
                                .expirationDate(LocalDateTime.now().plusMinutes(1))
                                .build())
                );

        // 토큰 업데이트
        recoveryToken.updateToken(token, LocalDateTime.now().plusMinutes(1));

        return token;
    }

    /**
     * 토큰으로 사용자 정보 찾기
     */
    public Member findMember(String token) {
        String email = getEmailFromToken(token);

        return memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFound("등록되지 않은 회원입니다."));
    }


    /**
     * 토큰이 유효한지 확인 후 토큰에서 이메일 정보 추출
     */
    public String getEmailFromToken(String token) {
        VerificationToken recoveryToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다."));

        if (recoveryToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(recoveryToken);
            throw new ExpiredTokenException("토큰이 만료되었습니다.");
        }

        return recoveryToken.getEmail();
    }
}