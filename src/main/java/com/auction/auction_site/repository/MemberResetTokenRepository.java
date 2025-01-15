package com.auction.auction_site.repository;

import com.auction.auction_site.entity.MemberResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberResetTokenRepository extends JpaRepository<MemberResetToken, Long> {

}
