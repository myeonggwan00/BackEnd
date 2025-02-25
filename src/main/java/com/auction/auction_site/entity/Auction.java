package com.auction.auction_site.entity;

import com.auction.auction_site.exception.EntityNotFound;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUCTION_ID")
    private Long id; // 경매 식별 아이디

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @OneToMany(mappedBy = "auction")
    private List<AuctionParticipant> participants;

    @OneToMany(mappedBy = "auction")
    private List<Bid> bids;

    private Long currentMaxPrice; // 경매 최고 입찰가

    private String auctionStatus; // 경매 상태

    private Long auctionWinner; // 경매 승리자

    private int auctionParticipantCount = 0; // 경매 참여자수

    private LocalDateTime startDate; // 경매 시작 날짜

    private LocalDateTime endDate; // 경매 종료 날짜

    // 경매 참여자수 증가
    public void increaseAuctionParticipantCount() {
        auctionParticipantCount++;
    }

    // 경매 참여자가 입찰한 경우 입찰 금액이 최고 입찰가인지 확인
    public void checkCurrentMaxPrice(Long bidAmount) {
        this.currentMaxPrice = this.currentMaxPrice > bidAmount ? this.currentMaxPrice : bidAmount;
    }

    // 경매 최고 입찰가 갱신
    public void updateCurrentMaxPrice(Long newMaxPrice) {
        this.currentMaxPrice = newMaxPrice != null ? newMaxPrice : 0;
    }

    // 경매에서 승리한 사람 결정
    public void determineFinalWinner() {
        if (bids == null || bids.isEmpty()) { // 경매에 입찰이 없는 경우
            return;
        }

        // 가장 큰 입찰 금액 구하기
        Bid highestBid = bids.stream()
                .max(Comparator.comparing(Bid::getBidAmount))
                .orElseThrow(() -> new EntityNotFound("해당 경매에 입찰한 기록이 없습니다."));

        // 가장 큰 입찰가를 입찰한 경매 참여자 조회 후 경매 승리자로 지정
        AuctionParticipant auctionParticipant = highestBid.getAuctionParticipant();
        this.auctionWinner = auctionParticipant.getMember().getId();
        auctionParticipant.configureWinner();
    }

    // 경매 승리자 변경
    public void changeWinner(Long participantId) {
        this.auctionWinner = participantId;
    }

    // 닉칠지기 결제 기간 내에 결제를 하지 않은 경우 다른 사람에게 우선권을 주도록 처리
    public void assignNextHighestBidder() {
        // 입찰 금액 기준 내림차순 정렬 후 반목문을 통해 우선권 처리
        List<Bid> bidList = bids.stream()
                .sorted(Comparator.comparing(Bid::getBidAmount, Comparator.reverseOrder()))
                .toList();

        for (Bid bid : bidList) {
            AuctionParticipant auctionParticipant = bid.getAuctionParticipant();

            if(auctionParticipant.getPaymentStatus().equals(PaymentStatus.NO_PAYMENT.getLabel())) {
                auctionParticipant.configureWinner();
                return;
            }
        }
    }

    // 경매 상태 변경
    public void changeAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus.getLabel();
        product.setProductStatus(false);
    }
}
