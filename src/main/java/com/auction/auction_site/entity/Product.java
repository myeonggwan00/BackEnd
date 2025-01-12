package com.auction.auction_site.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;


@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60, nullable = false)
    private String productName;
    @Column(length = 1000, nullable = false)
    private String productDetail;
    private Long startPrice;
    private Long bidStep;
    private Date auctionEndDate;
    @Builder.Default
    private Boolean productStatus = true;
    @CreatedDate
    @Column(updatable = false)
    private Date createdAt;
    @LastModifiedDate
    private Date updatedAt;
    private int viewCount;
    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private  Member member;
}
