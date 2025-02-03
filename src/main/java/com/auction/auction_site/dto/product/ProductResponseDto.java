package com.auction.auction_site.dto.product;

import com.auction.auction_site.dto.auction.AuctionDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductResponseDto {

    Long id;
    String productName;
    String productDetail;
    Long startPrice;
    Long bidStep;
    LocalDateTime auctionEndDate;
    Boolean productStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<String> imageUrls;
    String thumbnailUrl;
    int viewCount;
    int auctionParticipantCount; // 경매 참여자수
    Object auction; // 경매

    public void addAuctionDetails(AuctionDetailsDto auctionDetailsDto) {
        this.auction = auctionDetailsDto;
    }
}
