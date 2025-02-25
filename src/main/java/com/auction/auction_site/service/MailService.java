package com.auction.auction_site.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    /**
    * 메일 전송
    */
    public void sendPasswordResetEmail(String email, String token) {
        String resetLink = "http://localhost:8080/pwdhelp/reset-password?token=" + token; // 링크 클릭시 비밀번호 재설정창 이동

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText("<p>링크를 클릭하고 비밀번호를 재설정 하세요.:</p>" +
                    "<a href=\"" + resetLink + "\">비밀번호 재설정</a>", true); //true면 html

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송에 실패했습니다.", e);
        }
    }

    /**
     * 아이디 찾기용 메일 전송
     */
    public void sendLoginIdRecoveryEmail(String email, String token) {
        String link = "http://localhost:8080/members/id-recovery-email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            getCertificationMessage(helper, email, link);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송에 실패했습니다.", e);
        }
    }

    /**
     * 회원 가입시 본인인증용 메일 전송
     */
    public void sendVerificationEmail(String email, String token) {
        String link = "http://localhost:8080/members/registration-verification-email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            getCertificationMessage(helper, email, link);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송에 실패했습니다.", e);
        }

    }

    private void getCertificationMessage(MimeMessageHelper helper, String email, String link) throws MessagingException {
        helper.setTo(email);
        helper.setSubject("Auction 인증 메일입니다.");
        helper.setText("<h1 style='text-align: center;'>Auction 인증 메일입니다.</h1>" +
                            "<h3 style='text-align: center;'><a href='" + link + "'>인증 링크</a></h3>", true);
    }
}
