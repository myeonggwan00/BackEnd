package com.auction.auction_site.dto.auction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResponseBidDto {
    private LocalDateTime bidDate;
    private Long bidAmount;
    private String nickname;
}
