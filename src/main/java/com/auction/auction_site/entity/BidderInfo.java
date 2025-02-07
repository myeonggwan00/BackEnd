package com.auction.auction_site.entity;

import jakarta.persistence.*;
import lombok.*;

    @Entity
    @Table(name = "bidderInfo")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
    @AllArgsConstructor
    @Builder
    public class BidderInfo {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 50)
        private String name;

        @Column(nullable = false, length = 20)
        private String phoneNumber;

        @Column(nullable = false, length = 255)
        private String roadAddress;

        @Column(nullable = true, length = 255)
        private String detailAddress;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "bid_id", nullable = false, unique = true)
        private Bid bid;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "member_id", nullable = false, unique = true)
        private Member member;

        // Builder 사용 시 연관 관계 필드 포함된 생성자 추가
        @Builder
        public BidderInfo(String name, String phoneNumber, String roadAddress, String detailAddress, Bid bid, Member member) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.roadAddress = roadAddress;
            this.detailAddress = detailAddress;
            this.bid = bid;
            this.member = member;
        }

}
