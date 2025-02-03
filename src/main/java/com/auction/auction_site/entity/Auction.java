package com.auction.auction_site.entity;

import com.auction.auction_site.exception.EntityNotFound;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @OneToMany(mappedBy = "auction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AuctionParticipant> participants;

    @OneToMany(mappedBy = "auction")
    private List<Bid> bids;

    private Long currentMaxPrice;

    private String auctionStatus;

    private Long auctionWinner;

    private int auctionParticipantCount = 0;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public void increaseAuctionParticipantCount() {
        auctionParticipantCount++;
    }

    public void checkCurrentMaxPrice(Long bidAmount) {
        this.currentMaxPrice = this.currentMaxPrice > bidAmount ? this.currentMaxPrice : bidAmount;
    }

    public void updateCurrentMaxPrice(Long newMaxPrice) {
        this.currentMaxPrice = newMaxPrice != null ? newMaxPrice : 0;
    }

    public void determineFinalWinner() {
        if (bids == null || bids.isEmpty()) {
            return;
        }

        Bid highestBid = bids.stream()
                .max(Comparator.comparing(Bid::getBidAmount))
                .orElseThrow(() -> new EntityNotFound("해당 경매에 입찰한 기록이 없습니다."));

        AuctionParticipant auctionParticipant = highestBid.getAuctionParticipant();
        this.auctionWinner = auctionParticipant.getId();
        auctionParticipant.configureWinner();
    }

    public void changeWinner(Long participantId) {
        this.auctionWinner = participantId;
    }

    // 다음 순서
    public void assignNextHighestBidder() {
        // 입찰 금액 기준 내림차순 정렬 후, 동일한 사용자 제외
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

    public void changeAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus.getLabel();
        product.setProductStatus(true);
    }
}
