package com.auction.auction_site.service;

import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.MemberResetToken;
import com.auction.auction_site.repository.MemberRepository;
import com.auction.auction_site.repository.MemberResetTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {


    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberResetTokenRepository ResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


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

    /**
     * 비밀번호 재설정
     */
    public void resetPassword(String email, String token, String newPassword) {
        MemberResetToken resetToken = ResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

        if (!resetToken.getEmail().equals(email)) {
            throw new RuntimeException("이메일이 토큰과 일치하지 않습니다.");
        }

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("토큰이 만료되었습니다.");
        }

        // 비밀번호 변경
        Member member = memberRepository.findByEmail(email);
        member.setPassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);

        //토큰 삭제
        ResetTokenRepository.delete(resetToken);
    }
}
