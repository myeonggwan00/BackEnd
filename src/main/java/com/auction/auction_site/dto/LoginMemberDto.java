package com.auction.auction_site.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class LoginMemberDto {
    private String loginId;
    private String password;
}
