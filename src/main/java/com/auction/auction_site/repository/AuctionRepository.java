package com.auction.auction_site.repository;

import com.auction.auction_site.dto.product.ProductDto;
import com.auction.auction_site.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
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
            "WHERE m.loginId = :loginId")
    List<ProductDto> findBiddingProductsByLoginId(@Param("loginId") String loginId);

    @Query("SELECT DISTINCT new com.auction.auction_site.dto.product.ProductDto" +
            "(p.id, a.id, p.productName, p.productDetail, p.viewCount, a.auctionParticipantCount) " +
            "FROM Product p " +
            "JOIN Auction a ON p.id = a.product.id " +
            "JOIN AuctionParticipant ap ON ap.auction.id = a.id " +
            "JOIN Member m ON m.id = ap.member.id " +
            "WHERE m.loginId = :loginId AND a.auctionStatus = :auctionStatus")
    List<ProductDto> findOngoingBiddingProductsByLoginId(@Param("loginId") String loginId, @Param("auctionStatus") String auctionStatus);
}
