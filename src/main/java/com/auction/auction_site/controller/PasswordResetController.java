package com.auction.auction_site.controller;

import com.auction.auction_site.dto.ApiResponse;
import com.auction.auction_site.dto.mail.MailDto;
import com.auction.auction_site.dto.mail.PasswordRequestDto;
import com.auction.auction_site.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pwdhelp")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     *  uuid 생성, 메일 전송
     */
    @PostMapping("/send-mail")
    public ResponseEntity<ApiResponse> passwordRecoverySendEmail(@RequestBody MailDto mailDto){
        passwordResetService.
    }

}
