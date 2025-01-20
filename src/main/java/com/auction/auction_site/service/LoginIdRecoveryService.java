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

@Service
@RequiredArgsConstructor
@Transactional
public class LoginIdRecoveryService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final MemberRepository memberRepository;

    public void findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new EntityNotFound("이메일에 해당하는 사용자를 찾을 수 없습니다.");
        }
    }

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();

        VerificationToken recoveryToken = verificationTokenRepository.findByEmail(email)
                .orElseGet(() -> verificationTokenRepository.save(
                        VerificationToken.builder()
                                .token(token)
                                .email(email)
                                .expirationDate(LocalDateTime.now().plusMinutes(1))
                                .build())
                );

        recoveryToken.updateToken(token, LocalDateTime.now().plusMinutes(1));

        return token;
    }

    @Transactional(noRollbackFor = ExpiredTokenException.class)
    public Member findMember(String token) {
        String email = validateToken(token);

        Member member = memberRepository.findByEmail(email);

        if(member == null) {
            throw new EntityNotFound("등록되지 않은 회원입니다.");
        }

        return member;
    }

    public String validateToken(String token) {
        VerificationToken recoveryToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다."));

        String email = recoveryToken.getEmail();

        if (recoveryToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(recoveryToken);
            throw new ExpiredTokenException("토큰이 만료되었습니다.");
        }

        return email;
    }
}