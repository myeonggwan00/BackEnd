package com.auction.auction_site.dto.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequestDto {
    private String productName;
    private String productDetail;
    private Long startPrice;
    private Long bidStep;
    private Date auctionEndDate;
}
