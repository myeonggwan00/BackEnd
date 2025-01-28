package com.auction.auction_site.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductResponseDto {

    Long id;
    String productName;
    String productDetail;
    Long startPrice;
    Long bidStep;
    LocalDateTime auctionEndDate;
    Boolean productStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<String> imageUrls;
     String thumbnailUrl;
     int viewCount;
}
