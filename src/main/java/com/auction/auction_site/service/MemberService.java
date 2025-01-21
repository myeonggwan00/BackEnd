package com.auction.auction_site.service;

import com.auction.auction_site.dto.member.MemberDetailsDto;
import com.auction.auction_site.dto.member.MemberDto;
import com.auction.auction_site.dto.member.MemberResponseDto;
import com.auction.auction_site.dto.member.UpdateMemberDto;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.Product;
import com.auction.auction_site.entity.VerificationToken;
import com.auction.auction_site.exception.ExpiredTokenException;
import com.auction.auction_site.exception.InvalidTokenException;
import com.auction.auction_site.repository.MemberRepository;
import com.auction.auction_site.repository.ProductRepository;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.repository.VerificationTokenRepository;
import com.auction.auction_site.security.jwt.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
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

    public MemberDetailsDto getMemberDetails(String token) {
        String loginId = jwtUtil.getLoginId(token);
        Member member = memberRepository.findByLoginId(loginId);
        Long memberId = member.getId();

        // 2. 해당 회원이 등록한 모든 상품 조회
        List<Product> products = productRepository.findProductsByMemberId(memberId);

        // 3. 상품 상태별로 상품을 분류
        Map<Boolean, List<MemberDetailsDto.ProductDto>> groupedProducts = new HashMap<>();

        for (Product product : products) {
            boolean status = product.getProductStatus();  // "판매중" or "판매종료"
            groupedProducts.computeIfAbsent(status, k -> new ArrayList<>())
                    .add(new MemberDetailsDto.ProductDto(product.getId(), product.getProductName(), product.getProductDetail()));
        }

        // 4. 상태별 상품 DTO로 변환
        List<MemberDetailsDto.ProductStatusDto> productStatusDtos = new ArrayList<>();

        for (Map.Entry<Boolean, List<MemberDetailsDto.ProductDto>> entry : groupedProducts.entrySet()) {
            productStatusDtos.add(new MemberDetailsDto.ProductStatusDto(entry.getKey(), entry.getValue()));
        }

        // 5. 최종 DTO 반환
        return new MemberDetailsDto(member.getLoginId(), member.getNickname(), productStatusDtos);
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
}
