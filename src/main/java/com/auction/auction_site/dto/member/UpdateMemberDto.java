package com.auction.auction_site.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateMemberDto {
    private String nickname;
    private String password;
}
