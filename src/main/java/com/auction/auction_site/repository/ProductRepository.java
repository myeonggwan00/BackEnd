package com.auction.auction_site.repository;

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
    @Query("SELECT p FROM Product p WHERE p.member.id = :memberId")
    List<Product> findProductsByMemberId(@Param("memberId") Long memberId);

    // 기본 정렬 메서드
    Page<Product> findAllByOrderByAuctionEndDateAsc(Pageable pageable);
    Page<Product> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

}

