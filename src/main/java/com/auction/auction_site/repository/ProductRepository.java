package com.auction.auction_site.repository;

import com.auction.auction_site.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.member.id = :memberId")
    List<Product> findProductsByMemberId(@Param("memberId") Long memberId);
}

