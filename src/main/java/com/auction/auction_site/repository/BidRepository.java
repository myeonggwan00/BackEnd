package com.auction.auction_site.repository;

import com.auction.auction_site.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionId(Long auctionId);
    List<Bid> findByAuctionParticipantId(Long auctionParticipantId);
    @Query("SELECT max(b.bidAmount) FROM Bid b JOIN Auction a ON b.auction.id = a.id WHERE a.id = :auctionId")
    Long findMaxBidAmount(@Param("auctionId") Long auctionId);
}
