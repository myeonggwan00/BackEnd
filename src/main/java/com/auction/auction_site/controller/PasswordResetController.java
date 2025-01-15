package com.auction.auction_site.controller;


import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.dto.mail.MailDto;

import com.auction.auction_site.dto.mail.PasswordResetRequest;
import com.auction.auction_site.service.MailService;
import com.auction.auction_site.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        SuccessResponse response = SuccessResponse.success("비밀번호 재설정 메일이 발송되었습니다.", null);
        return ResponseEntity.ok(response);

    }

    /**
     *  비밀번호 재설정 페이지 로드 (GET 요청)
     */
    @GetMapping("/reset-password")
    public ResponseEntity<SuccessResponse> getResetPasswordPage(@RequestParam("token") String token) {
        // 토큰의 유효성 검증 또는 프론트엔드에서 처리할 토큰 전달
        return ResponseEntity.ok(SuccessResponse.success("비밀번호 재설정 페이지를 로드합니다.", null));
    }


    /**
     *  비밀번호 재설정
     */
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@RequestBody PasswordResetRequest resetRequest) {
        passwordResetService.resetPassword(resetRequest.getEmail(), resetRequest.getToken(), resetRequest.getNewPassword());

        SuccessResponse response = SuccessResponse.success("비밀번호가 성공적으로 변경되었습니다.", null);
        return ResponseEntity.ok(response);
    }

}
