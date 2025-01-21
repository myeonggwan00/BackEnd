package com.auction.auction_site.controller;

import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.dto.mail.MailDto;
import com.auction.auction_site.dto.member.MemberResponseDto;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.service.LoginIdRecoveryService;
import com.auction.auction_site.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LoginIdRecoveryController {
    private final LoginIdRecoveryService loginIdFindService;
    private final MailService mailService;

    @PostMapping("/email-verification")
    public ResponseEntity<?> sendEmail(@RequestBody MailDto mailDto) {
        loginIdFindService.findMemberByEmail(mailDto.getEmail());
        String token = loginIdFindService.createToken(mailDto.getEmail());
        mailService.sendLoginIdRecoveryEmail(mailDto.getEmail(), token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("이메일 인증 링크 전송 완료", null));
    }

    @GetMapping("/email-verification")
    public ResponseEntity<?> emailVerify(@RequestParam String token) {
        Member findMember = loginIdFindService.findMember(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success(
                        "아이디 찾기 완료",
                        MemberResponseDto.builder()
                                .loginId(findMember.getLoginId())
                                .nickname(findMember.getNickname())
                                .email(findMember.getEmail())
                                .role(findMember.getRole())
                                .build()));
    }
}

