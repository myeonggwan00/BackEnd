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

/**
 * 로그인 아이디를 찾기 위한 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/members/id-recovery-email")
@RequiredArgsConstructor
public class LoginIdRecoveryController {
    private final LoginIdRecoveryService loginIdFindService;
    private final MailService mailService;

    /**
     * 회원 이메일로 인증 링크를 포함한 이메일 전송
     */
    @PostMapping
    public ResponseEntity<?> sendEmail(@RequestBody MailDto mailDto) {
        loginIdFindService.validateEmailExists(mailDto.getEmail()); // 이메일 검증
        String token = loginIdFindService.createToken(mailDto.getEmail()); // 인증 처리용 토큰 생성
        mailService.sendLoginIdRecoveryEmail(mailDto.getEmail(), token); // 해당 이메일에 인증 링크를 포함한 이메일 전송

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("이메일 인증 링크 전송 완료", null));
    }

    /**
     * 회원이 인증 링크를 클릭했을 때 토큰 검증 후 아이디를 찾기
     */
    @GetMapping
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

