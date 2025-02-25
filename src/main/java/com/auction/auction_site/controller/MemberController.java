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

/**
 * 회원과 관련된 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MailService mailService;
    private final MemberService memberService;

    /**
     * 회원 정보를 조회
     * *
     * 1. progress
     * - true: 입찰한 상품 중에서 경매가 진행 중인 것만 보여주기
     * - false: 기본값, 입찰한 전체 상품 보여주기
     * *
     * 2. completed
     * - true: 판매한 상품 중에서 경매가 완료된 것만 보여주기
     * - false: 기본값, 판매한 상품 전체 보여주기
     */
    @GetMapping("/my-page")
    public ResponseEntity<SuccessResponse> getDetails(@RequestParam(defaultValue = "false") boolean progressOnly,
                                                      @RequestParam(defaultValue = "false") boolean completedOnly,
                                                      @RequestHeader("Authorization") String authorization) {
        MemberDetailsDto memberDetails = memberService.getMemberDetails(authorization, progressOnly, completedOnly);
        return ResponseEntity.ok().body(SuccessResponse.success("회원 정보 조회 완료", memberDetails));
    }

    /**
     * 회원 가입시 로그인 아이디 중복 검사
     */
    @GetMapping("/id-availability")
    public ResponseEntity<?> checkId(@RequestParam("value") String loginId) {
        if(memberService.checkLoginId(loginId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(SuccessResponse.success("아이디가 중복되었습니다.", null));
        }

        return ResponseEntity.ok().body(SuccessResponse.success("사용 가능한 아이디입니다.", null));
    }

    /**
     * 회원 가입시 닉네임 중복 검사
     */
    @GetMapping("/nickname-availability")
    public ResponseEntity<?> checkNickname(@RequestParam("value") String nickname) {
        if(memberService.checkNickname(nickname)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error("CONFLICT", "닉네임이 중복되었습니다."));
        }

        return ResponseEntity.ok().body(SuccessResponse.success("사용 가능한 닉네임 입니다.", null));
    }

    /**
     * 회원 가입시 본인 인증에 사용할 링크를 포함한 이메일 전송
     */
    @PostMapping("/registration-verification-email")
    public ResponseEntity<?> sendEmailVerification(@RequestBody MailDto mailDto) {
        String token = memberService.createToken(mailDto.getEmail());
        mailService.sendVerificationEmail(mailDto.getEmail(), token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("이메일 인증 링크 전송 완료", null));
    }

    /**
     * 인증 링크 눌렀을 때의 인증 처리
     */
    @GetMapping("/registration-verification-email")
    public ResponseEntity<?> checkEmailVerification(@RequestParam String tokenValue) {
        if(!memberService.verifyToken(tokenValue)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("AUTHENTICATION_FAILED", "본인 인증에 실패했습니다."));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("AUTHENTICATION_SUCCESS", "본인 인증에 성공했습니다."));
    }

    /**
     * 회원 가입
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
    @PatchMapping
    public ResponseEntity<SuccessResponse> modify(@RequestBody UpdateMemberDto updateMemberDto, @RequestHeader("Authorization") String authorization) {
        MemberResponseDto memberResponseDto = memberService.updateMember(updateMemberDto, authorization);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("회원 수정 완료", memberResponseDto));
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping
    public ResponseEntity<SuccessResponse> delete(@RequestHeader("Authorization") String authorization, HttpServletResponse response) {
        memberService.deleteProcess(authorization);

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success("회원 탈퇴 완료", null));

    }
}