package com.auction.auction_site.dto.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequestDto {
    private String productName;
    private String productDetail;
    private Long startPrice;
    private Long bidStep;
    private LocalDateTime auctionEndDate;
    private List<MultipartFile> productImage;
}
