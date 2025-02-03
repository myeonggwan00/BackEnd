package com.auction.auction_site.dto.member;

import com.auction.auction_site.dto.product.ProductDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class MemberDetailsDto {
    private String loginId;

    private String nickname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate registerDate;

    private Object bidingProducts;

    private Object soldProducts;

    public MemberDetailsDto(String loginId, String nickname, LocalDate registerDate, List<ProductDto> bidingProducts, List<ProductDto> soldProducts) {
        this.loginId = loginId;
        this.nickname = nickname;
        this.registerDate = registerDate;
        this.bidingProducts = bidingProducts;
        this.soldProducts = soldProducts;
    }
}
