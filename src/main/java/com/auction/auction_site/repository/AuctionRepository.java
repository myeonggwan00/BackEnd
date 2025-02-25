package com.auction.auction_site.repository;

import com.auction.auction_site.dto.product.ProductDto;
import com.auction.auction_site.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByEndDateBeforeAndAuctionStatus(LocalDateTime now, String auctionStatus);

    @Query("SELECT DISTINCT new com.auction.auction_site.dto.product.ProductDto" +
            "(p.id, a.id, p.productName, p.productDetail, p.viewCount, a.auctionParticipantCount) " +
            "FROM Product p " +
            "JOIN Auction a ON p.id = a.product.id " +
            "JOIN AuctionParticipant ap ON ap.auction.id = a.id " +
            "JOIN Member m ON m.id = ap.member.id " +
            "JOIN Bid b ON b.auctionParticipant.id = ap.id " +
            "WHERE m.loginId = :loginId")
    List<ProductDto> findBiddingProductsByLoginId(@Param("loginId") String loginId);

    @Query("SELECT DISTINCT new com.auction.auction_site.dto.product.ProductDto" +
            "(p.id, a.id, p.productName, p.productDetail, p.viewCount, a.auctionParticipantCount) " +
            "FROM Product p " +
            "JOIN Auction a ON p.id = a.product.id " +
            "JOIN AuctionParticipant ap ON ap.auction.id = a.id " +
            "JOIN Member m ON m.id = ap.member.id " +
            "JOIN Bid b ON b.auctionParticipant.id = ap.id " +
            "WHERE m.loginId = :loginId AND a.auctionStatus = :auctionStatus")
    List<ProductDto> findOngoingBiddingProductsByLoginId(@Param("loginId") String loginId, @Param("auctionStatus") String auctionStatus);

    @Modifying
    @Query("UPDATE Auction a SET a.auctionParticipantCount = a.auctionParticipantCount - 1 WHERE a.id IN (SELECT ap.auction.id FROM AuctionParticipant ap WHERE ap.member.id = :memberId) AND a.auctionParticipantCount > 0")
    void decreaseParticipantCountByMemberId(Long memberId);

    @Modifying
    @Query("DELETE FROM Auction a WHERE a.product.member.id = :memberId")
    void deleteByMemberId(Long memberId);
}
