package com.auction.auction_site.repository;

import com.auction.auction_site.entity.AuctionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionParticipantRepository extends JpaRepository<AuctionParticipant, Long> {
    Optional<AuctionParticipant> findByAuctionIdAndMemberId(Long auctionId, Long memberId);
    boolean existsByAuctionIdAndMemberId(Long auctionId, Long memberId);
    @Query("SELECT ap.auctionParticipantStatus FROM AuctionParticipant ap WHERE  ap.member.id = :memberId AND ap.auction.id = :auctionId")
    String findAuctionParticipantStatusByMemberId(Long memberId, Long auctionId);
    List<AuctionParticipant> findByPaymentDeadlineBeforeAndPaymentStatus(LocalDateTime paymentDeadlineBefore, String paymentStatus);

    @Modifying
    @Query("DELETE FROM AuctionParticipant ap WHERE ap.member.id = :memberId")
    void deleteByMemberId(Long memberId);

    @Modifying
    @Query("DELETE FROM AuctionParticipant ap WHERE ap.auction.id = :auctionId")
    void deleteByAuctionId(Long auctionId);
}
