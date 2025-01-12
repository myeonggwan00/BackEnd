package com.auction.auction_site.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Member {
    @Id
    @Column(name = "MEMBER_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 식별 고유 아이디

    @Column(length = 30, nullable = false)
    private String loginId; // 로그인시 사용되는 사용자 아이디
    @Column(length = 60, nullable = false)
    private String password; // 로그인시 사용되는 사용자 비밀번호
    @Column(length = 30, nullable = false)
    private String nickname; // 사용자 닉네임
    @Column(length = 100, nullable = false)
    private String email;



    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_USER; // 권한(사용자, 관리자)

    private LocalDate registerDate; // 회원가입 날짜

    public Member() {}
}
