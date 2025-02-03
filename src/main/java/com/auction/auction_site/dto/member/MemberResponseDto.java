package com.auction.auction_site.dto.member;

import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Getter @Setter
public class MemberResponseDto {
    private String loginId;
    private String nickname;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate registerDate;
    private Role role;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .registerDate(member.getRegisterDate())
                .role(member.getRole())
                .build();
    }
}
