package com.auction.auction_site.controller;


import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.dto.mail.MailDto;

import com.auction.auction_site.service.MailService;
import com.auction.auction_site.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pwdhelp")
@RequiredArgsConstructor
public class PasswordResetController {

    @Autowired
    private final PasswordResetService passwordResetService;
    @Autowired
    private final MailService mailService;

    /**
     *  uuid 생성, 메일 전송
     */
    @PostMapping("/send-mail")
    public ResponseEntity<SuccessResponse> passwordRecoverySendEmail(@RequestBody MailDto mailDto){
        passwordResetService.findMemberByEmail(mailDto.getEmail());
        String token = passwordResetService.createPasswordResetToken(mailDto.getEmail());
        mailService.sendPasswordResetEmail(mailDto.getEmail(), token);

        SuccessResponse response = SuccessResponse.success("패스워드 재설정 메일이 발송되었습니다.", null);
        return ResponseEntity.ok(response);

    }

}
