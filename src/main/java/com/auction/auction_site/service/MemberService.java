package com.auction.auction_site.service;

import com.auction.auction_site.dto.member.MemberDetailsDto;
import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.dto.member.MemberResponseDto;
import com.auction.auction_site.dto.member.UpdateMemberDto;
import com.auction.auction_site.dto.product.ProductDto;
import com.auction.auction_site.entity.*;
import com.auction.auction_site.exception.ExpiredTokenException;
import com.auction.auction_site.exception.InvalidTokenException;
import com.auction.auction_site.repository.*;
import com.auction.auction_site.security.jwt.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.auction.auction_site.entity.AuctionStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionParticipantRepository auctionParticipantRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public boolean checkLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public boolean checkNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean checkEmail(String token) {
        VerificationToken verificationToken = validateToken(token);

        boolean isVerified = verificationToken.verifyToken(token);

        verificationTokenRepository.delete(verificationToken);

        return isVerified;
    }

    /**
     * 필터링
     */
    public MemberDetailsDto getMemberDetails(String authorization , boolean progressOnly, boolean completedOnly) {
        String token = authorization.split(" ")[1];
        String loginId = jwtUtil.getLoginId(token);
        List<ProductDto> bidProducts = null;
        List<ProductDto> sellProducts = null;

        if(progressOnly) {
            bidProducts = auctionRepository.findOngoingBiddingProductsByLoginId(loginId, RUNNING.getLabel());
        } else {
            bidProducts = auctionRepository.findBiddingProductsByLoginId(loginId);
        }

        if(completedOnly) {
            sellProducts = productRepository.findSoldProductsByLoginId(loginId, FINISHED.getLabel());
        } else {
            sellProducts = productRepository.findProductsByLoginId(loginId);
        }

        Member member = memberRepository.findByLoginId(loginId);


        return new MemberDetailsDto(member.getLoginId(), member.getNickname(), member.getRegisterDate(),bidProducts, sellProducts);
    }

    public MemberResponseDto registerProcess(MemberDto memberDto) {
        if(memberRepository.existsByLoginId(memberDto.getLoginId())) { // 중복 검사
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        Member member = Member.builder()
                .loginId(memberDto.getLoginId())
                .password(bCryptPasswordEncoder.encode(memberDto.getPassword()))
                .nickname(memberDto.getNickname())
                .email(memberDto.getEmail())
                .registerDate(LocalDate.now())
                .build();

        memberRepository.save(member);

        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto updateMember(UpdateMemberDto updateMemberDto, String token) {
        String loginId = jwtUtil.getLoginId(token);

        Member member = memberRepository.findByLoginId(loginId);

        member.setNickname(updateMemberDto.getNickname());
        member.setPassword(bCryptPasswordEncoder.encode(updateMemberDto.getPassword()));

        return MemberResponseDto.from(member);
    }

    @Transactional
    public void deleteProcess(String token) {
        String loginId = jwtUtil.getLoginId(token);

        Member findMember = memberRepository.findByLoginId(loginId);

        memberRepository.delete(findMember);
        refreshTokenRepository.deleteByLoginId(loginId);
    }

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();

        VerificationToken recoveryToken = verificationTokenRepository.findByEmail(email)
                .orElseGet(() -> verificationTokenRepository.save(
                        VerificationToken.builder()
                                .token(token)
                                .email(email)
                                .expirationDate(LocalDateTime.now().plusMinutes(5))
                                .build())
                );

        recoveryToken.updateToken(token, LocalDateTime.now().plusMinutes(5));

        return token;
    }

    public VerificationToken validateToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다."));

        if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new ExpiredTokenException("토큰이 만료되었습니다.");
        }

        return verificationToken;
    }

    public Member getMember(String authorization) {
        // 토큰에서 사용자 정보 가져오기
        String token = authorization.split(" ")[1];
        String loginId = jwtUtil.getLoginId(token);

        return memberRepository.findByLoginId(loginId);
    }
}
