package com.auction.auction_site.repository;

import com.auction.auction_site.dto.product.ProductDto;
import com.auction.auction_site.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // 경매 참여자 순으로 경매 상품 필터링
    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN Auction a ON p.id = a.product.id " +
            "ORDER BY a.auctionParticipantCount DESC")
    Page<Product> findAllByOrderedByParticipants(Pageable pageable);

    // 기본 정렬 메서드
    Page<Product> findAllByOrderByAuctionEndDateAsc(Pageable pageable);
    Page<Product> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

}

