package com.auction.auction_site.dto;

import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter @Setter
public class MemberDto {
    private String loginId;

    private String password;

    private String name;

    private String nickname;

    private Role role;

    private String email;

    public MemberDto() {}

    // 엔티티 → DTO 변환 메서드
    public static MemberDto fromMember(Member member) {
        return MemberDto.builder()
                .loginId(member.getLoginId())
                .password(member.getPassword())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}
