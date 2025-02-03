package com.auction.auction_site.repository;

import com.auction.auction_site.entity.AuctionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuctionParticipantRepository extends JpaRepository<AuctionParticipant, Long> {
    AuctionParticipant findByAuctionIdAndMemberId(Long auctionId, Long memberId);
    AuctionParticipant findByMemberId(Long memberId);
    AuctionParticipant findByPaymentDeadlineBeforeAndPaymentStatus(LocalDateTime paymentDeadlineBefore, String paymentStatus);
}
