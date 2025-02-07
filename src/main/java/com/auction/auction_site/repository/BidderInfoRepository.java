package com.auction.auction_site.repository;

import com.auction.auction_site.entity.BidderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidderInfoRepository extends JpaRepository<BidderInfo, Long> {
}
