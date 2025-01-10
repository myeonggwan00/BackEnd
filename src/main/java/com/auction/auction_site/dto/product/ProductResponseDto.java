package com.auction.auction_site.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
    Date auctionEndDate;
    Boolean productStatus;
    Date createdAt;
    Date updatedAt;
    int viewCount;
}
