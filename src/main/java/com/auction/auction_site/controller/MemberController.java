package com.auction.auction_site.controller;

import com.auction.auction_site.dto.ErrorResponse;
import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.dto.mail.MailDto;
import com.auction.auction_site.dto.member.MemberDetailsDto;
import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.dto.member.MemberResponseDto;
import com.auction.auction_site.dto.member.UpdateMemberDto;
import com.auction.auction_site.service.MailService;
import com.auction.auction_site.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MailService mailService;
    private final MemberService memberService;

    /**
     * 회원 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getDetails(@RequestHeader("Authorization") String authorization) {
        String token = authorization.split(" ")[1];
        MemberDetailsDto memberDetails = memberService.getMemberDetails(token);
        return ResponseEntity.ok().body(SuccessResponse.success("회웑 정보 조회 완료", memberDetails));
    }

    @GetMapping("/id")
    public ResponseEntity<?> checkId(@RequestParam("value") String loginId) {
        if(memberService.checkLoginId(loginId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(SuccessResponse.success("아이디가 중복되었습니다.", null));
        }

        return ResponseEntity.ok().body(SuccessResponse.success("사용 가능한 아이디입니다.", null));
    }

    @GetMapping("/nickname")
    public ResponseEntity<?> checkNickname(@RequestParam("value") String nickname) {
        if(memberService.checkNickname(nickname)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error("CONFLICT", "닉네임이 중복되었습니다."));
        }

        return ResponseEntity.ok().body(SuccessResponse.success("사용 가능한 닉네임 입니다.", null));
    }

    @PostMapping("/email-verification")
    public ResponseEntity<?> sendEmailVerification(@RequestBody MailDto mailDto) {
        System.out.println("email = " + mailDto.getEmail());
        String token = memberService.createToken(mailDto.getEmail());
        mailService.sendVerificationEmail(mailDto.getEmail(), token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("이메일 인증 링크 전송 완료", null));
    }

    @GetMapping("/email-verification")
    public ResponseEntity<?> checkEmailVerification(@RequestParam String token) {

        boolean checkEmail = memberService.checkEmail(token);

        if(!checkEmail) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("AUTHENTICATION_FAILED", "본인 인증에 실패했습니다."));
        }


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("AUTHENTICATION_SUCCESS", "본인 인증에 성공했습니다."));
    }

    /**
     * 회원 등록
     */
    @PostMapping
    public ResponseEntity<SuccessResponse> register(@RequestBody MemberDto memberDto) {
        MemberResponseDto memberResponseDto = memberService.registerProcess(memberDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success("회원가입 완료", memberResponseDto));
    }

    /**
     * 회원 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<SuccessResponse> modify(@RequestBody UpdateMemberDto updateMemberDto, @RequestHeader("Authorization") String authorization) {
        String token = authorization.split(" ")[1];
        MemberResponseDto memberResponseDto = memberService.updateMember(updateMemberDto, token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("회원 수정 완료", memberResponseDto));
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse> delete(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        String token = authorization.split(" ")[1];

        memberService.deleteProcess(token);


        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success("회원 탈퇴 완료", null));

    }
}