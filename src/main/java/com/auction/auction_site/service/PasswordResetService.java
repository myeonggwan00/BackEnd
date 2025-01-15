package com.auction.auction_site.service;

import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.MemberResetToken;
import com.auction.auction_site.repository.MemberRepository;
import com.auction.auction_site.repository.MemberResetTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberResetTokenRepository ResetTokenRepository;


    /**
     *  이메일 확인
     */
    public Member findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new RuntimeException("이메일에 해당하는 사용자를 찾을 수 업습니다. : " + email);
        }
        return member;
    }


    /**
     * uuid 토큰 생성, 이메일을 db에 저장
     */
    public String createPasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();

        MemberResetToken resetToken = new MemberResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpirationDate(LocalDateTime.now().plusHours(1)); // 1시간 유효

        ResetTokenRepository.save(resetToken);

        return token;
    }



}
