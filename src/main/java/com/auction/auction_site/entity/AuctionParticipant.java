package com.auction.auction_site.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUCTIONPARTICIPANT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUCTION_ID")
    private Auction auction;

    @Builder.Default
    private String auctionParticipantStatus = AuctionParticipantStatus.PENDING.getLabel();

    @Column
    @Builder.Default
    private String paymentStatus = PaymentStatus.NO_PAYMENT.getLabel();

    private LocalDateTime paymentDeadline;

    public static AuctionParticipant participant(Member member, Auction auction) {
        AuctionParticipant auctionParticipant = AuctionParticipant.builder().member(member).auction(auction).build();
        auction.getParticipants().add(auctionParticipant);
        auction.increaseAuctionParticipantCount();
        return auctionParticipant;
    }

    // 경매에서 승리한 회원에 대한 추가 설정
    public void configureWinner() {
        this.auctionParticipantStatus = AuctionParticipantStatus.WINNER.getLabel();
        this.paymentStatus = PaymentStatus.PENDING.getLabel();
        this.paymentDeadline = LocalDateTime.now().plusHours(24);
        this.auction.changeWinner(this.id);
    }

    public void processExpiredPayment() {
        this.paymentStatus = PaymentStatus.EXPIRED.getLabel();
        this.auctionParticipantStatus = AuctionParticipantStatus.PENDING.getLabel();
        this.auction.assignNextHighestBidder();

    }
}
