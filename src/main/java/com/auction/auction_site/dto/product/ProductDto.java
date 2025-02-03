package com.auction.auction_site.dto.product;

import lombok.Getter;

@Getter
public class ProductDto {
    private final Long productId;
    private final Long auctionId;
    private final String productName;
    private final String productDetail;
    private final int viewCount;
    private final int auctionParticipantCount;

    public ProductDto(Long productId, Long auctionId, String productName, String productDetail, int viewCount, int auctionParticipantCount) {
        this.productId = productId;
        this.auctionId = auctionId;
        this.productName = productName;
        this.productDetail = productDetail;
        this.viewCount = viewCount;
        this.auctionParticipantCount = auctionParticipantCount;
    }
}
