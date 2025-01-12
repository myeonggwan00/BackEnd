package com.auction.auction_site.dto.member;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MemberDetailsDto {
    private String loginId;

    private String nickname;

    private List<ProductStatusDto> products;

    public MemberDetailsDto(String loginId, String nickname, List<ProductStatusDto> products) {
        this.loginId = loginId;
        this.nickname = nickname;
        this.products = products;
    }

    // getters and setters

    @Getter @Setter
    public static class ProductStatusDto {
        private boolean status;
        private List<ProductDto> products;

        public ProductStatusDto(boolean status, List<ProductDto> products) {
            this.status = status;
            this.products = products;
        }
    }

    @Getter @Setter
    public static class ProductDto {
        private Long id;
        private String productName;
        private String productDetail;

        public ProductDto(Long id, String productName, String productDetail) {
            this.id = id;
            this.productName = productName;
            this.productDetail = productDetail;
        }
    }
}
