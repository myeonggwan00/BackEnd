package com.auction.auction_site.entity;


import com.auction.auction_site.dto.auction.ResponseBidDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BID_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUCTION_ID")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUCTIONPARTICIPANT_ID")
    private AuctionParticipant auctionParticipant;

    private Long bidAmount;

    private LocalDateTime bidDate;

    // 입찰 정보 추가
    public static Bid addBid(Auction auction, AuctionParticipant auctionParticipant, Long amount) {
        Bid bid = Bid.builder()
                .auction(auction)
                .auctionParticipant(auctionParticipant)
                .bidAmount(amount)
                .bidDate(LocalDateTime.now())
                .build();

        auction.getBids().add(bid);

        return bid;
    }

    // Bid → ResponseBidDto
    public ResponseBidDto fromBid() {
        return new ResponseBidDto(this.getBidDate(), this.getBidAmount(), this.auctionParticipant.getMember().getNickname());
    }
}
