package com.auction.auction_site.repository;

import com.auction.auction_site.dto.product.ProductDto;
import com.auction.auction_site.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT DISTINCT new com.auction.auction_site.dto.product.ProductDto" +
            "(p.id, a.id, p.productName, p.productDetail, p.viewCount, a.auctionParticipantCount) " +
            "FROM Product p " +
            "JOIN Auction a ON a.product.id = p.id " +
            "JOIN Member m ON p.member.id = m.id " +
            "WHERE m.loginId = :loginId")
    List<ProductDto> findProductsByLoginId(@Param("loginId") String loginId);

    @Query("SELECT DISTINCT new com.auction.auction_site.dto.product.ProductDto" +
            "(p.id, a.id, p.productName, p.productDetail, p.viewCount, a.auctionParticipantCount) " +
            "FROM Product p " +
            "JOIN Auction a ON a.product.id = p.id " +
            "JOIN Member m ON p.member.id = m.id " +
            "WHERE m.loginId = :loginId AND a.auctionStatus = :auctionStatus")
    List<ProductDto> findSoldProductsByLoginId(@Param("loginId") String loginId, @Param("auctionStatus") String auctionStatus);
    
    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN Auction a ON p.id = a.product.id " +
            "LEFT JOIN AuctionParticipant ap ON ap.auction.id = a.id " +  // <-- 여기
            "GROUP BY p.id " +
            "ORDER BY COUNT(ap) DESC")
    Page<Product> findAllByOrderedByParticipants(Pageable pageable);

    @Modifying
    @Query("DELETE FROM Product p WHERE p.member.id = :memberId")
    void deleteByMemberId(Long memberId);

    // 기본 정렬 메서드
    Page<Product> findAllByOrderByAuctionEndDateAsc(Pageable pageable);
    Page<Product> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

}

