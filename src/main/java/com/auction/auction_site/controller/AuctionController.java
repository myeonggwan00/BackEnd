package com.auction.auction_site.controller;

import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.dto.auction.*;
import com.auction.auction_site.dto.product.ProductResponseDto;
import com.auction.auction_site.entity.*;
import com.auction.auction_site.service.AuctionService;
import com.auction.auction_site.service.MemberService;
import com.auction.auction_site.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/products/{productId}/auction")
@RequiredArgsConstructor
public class AuctionController {
    private final MemberService memberService;
    private final AuctionService auctionService;
    private final ProductService productService;

    /**
     * 경매 참여
     */
    @PostMapping("/participate")
    public ResponseEntity<?> participateAuction(@PathVariable("productId") Long productId,
                                                @RequestHeader("Authorization") String authorization) {
        // 요청에서 JwtAccessToken 가져와서 회원 정보 가져오기
        Member member = memberService.getMember(authorization);

        // 경매 참여
        AuctionParticipant auctionParticipant = auctionService.participateAuction(member, productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("경매 참여 성공", AuctionParticipantDto.from(auctionParticipant)));
    }

    /**
     * 경매 입찰
     */
    @PostMapping("/bids")
    public ResponseEntity<?> participantAuction(@PathVariable Long productId, @RequestBody RequestBidDto requestBidDto,
                                                @RequestHeader("Authorization") String authorization) {
        Member member = memberService.getMember(authorization);

        productService.checkProduct(productId);

        AuctionDetailsDto auctionDetailsDto = auctionService.auctionBid(member, productId, requestBidDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success("입찰 성공", auctionDetailsDto));
    }

    /**
     * 입찰 내역 조회
     */
    @GetMapping("/bids")
    public ResponseEntity<?> details(@PathVariable Long productId,
                                     @RequestHeader("Authorization") String authorization) {
        Member member = memberService.getMember(authorization);

        ProductResponseDto productDetail = productService.productDetail(productId);

        AuctionDetailsDto auctionDetailsDto = auctionService.getBidsByAuction(member, productId);

        productDetail.addAuctionDetails(auctionDetailsDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("특정 상품 조회 완료", productDetail));
    }

    /**
     * 입찰 취소
     */
    @DeleteMapping("/bids")
    public ResponseEntity<?> deleteAuction(@PathVariable Long productId,
                                           @RequestHeader("Authorization") String authorization) {
        Member member = memberService.getMember(authorization);

        auctionService.cancelBids(member, productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.success("입찰이 성공적으로 취소되었습니다.", null));
    }
}
