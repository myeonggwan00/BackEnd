package com.auction.auction_site.dto.auction;

import com.auction.auction_site.entity.AuctionParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuctionParticipantDto {
    private String nickname;

    public static AuctionParticipantDto from(AuctionParticipant auctionParticipant) {
        return AuctionParticipantDto.builder()
                .nickname(auctionParticipant.getMember().getNickname())
                .build();
    }
}
