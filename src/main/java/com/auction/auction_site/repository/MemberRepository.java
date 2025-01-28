package com.auction.auction_site.repository;

import com.auction.auction_site.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickName);

    Member findByLoginId(String loginId);

    Member findByEmail(String email);
}