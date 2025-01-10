package com.auction.auction_site.controller;

import com.auction.auction_site.dto.ApiResponse;
import com.auction.auction_site.dto.member.MemberDetailsDto;
import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.dto.member.UpdateMemberDto;
import com.auction.auction_site.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 회원 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getDetails(@RequestHeader("Authorization") String authorization) {
        String token = authorization.split(" ")[1];
        MemberDetailsDto memberDetails = memberService.getMemberDetails(token);
        return ResponseEntity.ok(new ApiResponse("success", "회원 정보 조회 완료", memberDetails));
    }

    @GetMapping("/id")
    public ResponseEntity<?> checkId(@RequestParam("value") String loginId) {
        ApiResponse response = new ApiResponse();

        if(memberService.checkLoginId(loginId)) {
            response.setMessage("아이디가 중복되었습니다.");
            response.setData(null);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        response.setMessage("사용 가능한 아이디입니다.");
        response.setData(null);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/nickname")
    public ResponseEntity<?> checkNickname(@RequestParam("value") String nickname) {
        ApiResponse response = new ApiResponse();

        if(memberService.checkNickname(nickname)) {
            response.setMessage("닉네임이 중복되었습니다.");
            response.setData(null);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        response.setMessage("사용 가능한 닉네임입니다.");
        response.setData(null);

        return ResponseEntity.ok().body(response);
    }

    /**
     * 회원 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse> register(@RequestBody MemberDto memberDto) {
        MemberDto member = memberService.registerProcess(memberDto);

        // ApiResponse 생성
        ApiResponse response = new ApiResponse(
                "success",
                "회원가입 완료", // message
                Map.of( // 사용자 데이터
                        "loginId", member.getLoginId(),
                        "nickname", member.getNickname(),
                        "role", member.getRole()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 회원 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse> modify(@RequestBody UpdateMemberDto updateMemberDto, @RequestHeader("Authorization") String authorization) {
        String token = authorization.split(" ")[1];
        MemberDto memberDto = memberService.updateMember(updateMemberDto, token);

        // ApiResponse 생성
        ApiResponse response = new ApiResponse(
                "success",
                "회원수정 완료", // message
                Map.of( // 사용자 데이터
                        "loginId", memberDto.getLoginId(),
                        "nickname", memberDto.getNickname(),
                        "role", memberDto.getRole()
                )
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> delete(@RequestHeader("Authorization") String authorization, HttpServletResponse response) throws IOException {
        String token = authorization.split(" ")[1];

        memberService.deleteProcess(token);


        ApiResponse apiResponse = new ApiResponse("success", "회원 탈퇴 완료", null);

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

    }
}