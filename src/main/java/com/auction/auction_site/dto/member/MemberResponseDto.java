package com.auction.auction_site.dto.member;

import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter @Setter
public class MemberResponseDto {
    private String loginId;
    private String nickname;
    private String email;
    private Role role;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}
