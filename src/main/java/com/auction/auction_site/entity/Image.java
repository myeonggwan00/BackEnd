package com.auction.auction_site.entity;

import jakarta.persistence.*;
import jdk.jfr.StackTrace;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMAGE_ID")
    private Long id;

   // @Column(length = 300, nullable = false)
   @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String originFileName;  //파일 원본명

    @Column(nullable = false)
    private String filePath;



    @ManyToOne( cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}
