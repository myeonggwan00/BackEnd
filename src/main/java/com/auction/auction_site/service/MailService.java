package com.auction.auction_site.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {


    @Autowired
    private  final  JavaMailSender mailSender;
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


}
