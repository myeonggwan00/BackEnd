package com.auction.auction_site.entity;

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

    /**
     * 경매 종료 후에 경매에서 승리한 회원의 경우 상태를 승리자로 설정
     * 경매에서 승리한 회원 이외의 회원의 상태를 패배자가 아닌 보류(기본값)로 설정
     * 경매가 종료되기 전까지 경매 참여자의 상태를 보류(기본값)로 설정
     */
    @Builder.Default
    private String auctionParticipantStatus = AuctionParticipantStatus.PENDING.getLabel();


    /**
     * 경매가 종료되기 전까지 모든 회원의 결제 상태를 결제불가(기본값)로 설정
     * 경매에서 승리한 회원의 경우 결제 상태를 대기로 설정
     * 하지만 결제기간 내에 결제를 하지 않을시 결제 상태를 만료로 설정
     */
    @Builder.Default
    private String paymentStatus = PaymentStatus.NO_PAYMENT.getLabel();

    private LocalDateTime paymentDeadline;

    // 경매 참여
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
//        this.paymentDeadline = LocalDateTime.now().plusHours(24);
        this.paymentDeadline = LocalDateTime.now().plusMinutes(1);
        this.auction.changeWinner(this.getMember().getId());
    }

    // 경매에서 승리한 회원이 결제 기간 내에 결제하지 않을시 해당 회원에 대한 추가 설정
    public void processExpiredPayment() {
        this.paymentStatus = PaymentStatus.EXPIRED.getLabel(); // 결제 상태를 만료로 설정
        this.auctionParticipantStatus = AuctionParticipantStatus.PENDING.getLabel(); // 참여자 상태를 보류로 설정
        this.auction.assignNextHighestBidder(); // 다른 참여자에게 우선권 제공
    }
}
