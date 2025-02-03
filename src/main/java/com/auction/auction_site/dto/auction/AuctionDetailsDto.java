package com.auction.auction_site.dto.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AuctionDetailsDto {
    private int auctionParticipants;
    private String remainingAuctionTime;
    private List<ResponseBidDto> bids;
    private String auctionParticipantStatus;
    private String auctionStatus;
}
