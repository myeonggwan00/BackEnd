package com.auction.auction_site.controller;

import com.auction.auction_site.dto.ErrorResponse;
import com.auction.auction_site.dto.bidderInfo.BidderInfoDto;
import com.auction.auction_site.security.oauth.CustomOAuth2User;
import com.auction.auction_site.service.BidderInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bidder-info")
@RequiredArgsConstructor
public class BidderInfoController {

    private final BidderInfoService bidderInfoService;

    @PostMapping
    public ResponseEntity<BidderInfoDto> createBidderInfo(@RequestBody BidderInfoDto dto) {
        String loginId = ((CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getLoginId();
        if (loginId == null || loginId.trim().isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus("FAIL");
            errorResponse.setCode("Unauthorized");
            errorResponse.setMessage("로그인 정보가 없습니다.");
        }

        return ResponseEntity.ok(bidderInfoService.createBidderInfo(dto, loginId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidderInfoDto> getBidderInfo(@PathVariable Long id) {
        return ResponseEntity.ok(bidderInfoService.getBidderInfo(id));
    }
}
