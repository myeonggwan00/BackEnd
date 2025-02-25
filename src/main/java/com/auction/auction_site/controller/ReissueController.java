package com.auction.auction_site.controller;

import com.auction.auction_site.service.ReissueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

/**
 * Refresh 토큰을 받아 새로운 Access 토큰과 Refresh 토큰을 생성하는 ReissueController
 */
@RestController
@RequiredArgsConstructor
public class ReissueController {
    private final ReissueService reissueService;

    /**
     * 토큰 재발행
     */
    @PostMapping("/reissue")
    public void reissue(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Optional<String> refreshToken = reissueService.validateAndGetRefreshToken(request, response);

        if(refreshToken.isEmpty()) { return; }

        reissueService.reissueToken(response, refreshToken.get());
    }

}