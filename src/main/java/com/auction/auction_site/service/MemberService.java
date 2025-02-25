package com.auction.auction_site.service;

import com.auction.auction_site.dto.member.MemberDetailsDto;
import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.dto.member.MemberResponseDto;
import com.auction.auction_site.dto.member.UpdateMemberDto;
import com.auction.auction_site.dto.product.ProductDto;
import com.auction.auction_site.entity.*;
import com.auction.auction_site.exception.EntityNotFound;
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

/**
 * 회원과 관련된 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionParticipantRepository auctionParticipantRepository;
    private final BidRepository bidRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * 로그인 아이디 중복 검사
     */
    public boolean checkLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId); // 회원 가입시 입력한 로그인 아이디에 해당하는 회원이 존재하는 확인
    }

    /**
     * 닉네임 중복 검사
     */
    public boolean checkNickname(String nickname) {
        return memberRepository.existsByNickname(nickname); // 회원 가입시 입력한 닉네임에 해당하는 회원이 존재하는지 확인
    }

    /**
     * 토큰을 검증하는 메서드
     */
    public boolean verifyToken(String tokenValue) {
        VerificationToken verificationToken = validateTokenValue(tokenValue); // 토큰 값 검사

        boolean isVerified = verificationToken.verifyToken(tokenValue);

        verificationTokenRepository.delete(verificationToken);

        return isVerified;
    }

    /**
     * 회원이 등록 및 입찰한 상품/경매 정보 필터링
     */
    public MemberDetailsDto getMemberDetails(String authorization , boolean progressOnly, boolean completedOnly) {
        Member member = getMember(authorization);

        List<ProductDto> bidProducts = null;
        List<ProductDto> soldProducts = null;

        if(progressOnly) {
            bidProducts = auctionRepository.findOngoingBiddingProductsByLoginId(member.getLoginId(), RUNNING.getLabel());
        } else {
            bidProducts = auctionRepository.findBiddingProductsByLoginId(member.getLoginId());
        }

        if(completedOnly) {
            soldProducts = productRepository.findSoldProductsByLoginId(member.getLoginId(), FINISHED.getLabel());
        } else {
            soldProducts = productRepository.findProductsByLoginId(member.getLoginId());
        }

        return new MemberDetailsDto(member.getLoginId(), member.getNickname(), member.getRegisterDate(), bidProducts, soldProducts);
    }

    /**
     * 회원 가입
     */
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

    /**
     * 회원 수정
     */
    @Transactional
    public MemberResponseDto updateMember(UpdateMemberDto updateMemberDto, String authorization) {
        Member member = getMember(authorization);

        member.updateMember(updateMemberDto.getNickname(), bCryptPasswordEncoder.encode(updateMemberDto.getPassword()));

        return MemberResponseDto.from(member);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteProcess(String authorization) {
        Member member = getMember(authorization);

        auctionRepository.decreaseParticipantCountByMemberId(member.getId()); // 참여중인 경매의 참여자수 감소
        bidRepository.deleteByMemberId(member.getId()); // 입찰 내역 삭제
        auctionParticipantRepository.deleteByMemberId(member.getId()); // 경매 참여 정보 삭제
        auctionRepository.deleteByMemberId(member.getId());
        productRepository.deleteByMemberId(member.getId()); // 회원이 등록한 상품 삭제
        memberRepository.delete(member); // 회원 삭제
        refreshTokenRepository.deleteByLoginId(member.getLoginId());
    }

    /**
     * 본인 인증 처리에 필요한 토큰 생성
     * 본인 인증 요청할 때마다 토큰 값을 새로 생성하므로 이메일에 해당하는 인증용 토큰이 있으면? 새로 생성된 토큰값으로 갱신
     * 이메일에 해당하는 토큰이 없으면? 생성된 토큰 값으로 인증용 토큰 생성
     */
    @Transactional
    public String createToken(String email) {
        String tokenValue = UUID.randomUUID().toString(); // UUID 이용하여 토큰 값 생성

        verificationTokenRepository
                .findByEmail(email)
                .ifPresentOrElse(
                        existingToken -> existingToken.updateToken(tokenValue, LocalDateTime.now().plusMinutes(5)), // 기존 토큰이 있을 때만 실행
                        () -> verificationTokenRepository.save( // 기존 토큰이 없을 때만 실행
                                VerificationToken.builder()
                                        .token(tokenValue)
                                        .email(email)
                                        .expirationDate(LocalDateTime.now().plusMinutes(5))
                                        .build()
                        )
                );

        return tokenValue;
    }

    /**
     * 토큰 값 검사
     * 토큰 값 검사에 성고하면 인증용 토큰 반환, 실패하면 예외 발생
     */
    public VerificationToken validateTokenValue(String tokenValue) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다."));

        if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new ExpiredTokenException("토큰이 만료되었습니다.");
        }

        return verificationToken;
    }

    /**
     * Authorization 헤더에 담긴 JWT 토큰을 통해 회원 정보 가져오기
     */
    public Member getMember(String authorization) {
        String token = authorization.split(" ")[1]; // Authorization 헤더에서 토큰 추출
        String loginId = jwtUtil.getLoginId(token); // 토큰에서 사용자 정보 가져오기

        return memberRepository.findByLoginId(loginId).orElseThrow(() -> new EntityNotFound("가입되지 않은 회웝입니다."));
    }
}
