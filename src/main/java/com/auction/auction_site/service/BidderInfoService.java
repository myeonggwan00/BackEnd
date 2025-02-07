package com.auction.auction_site.service;

import com.auction.auction_site.dto.bidderInfo.BidderInfoDto;
import com.auction.auction_site.entity.Bid;
import com.auction.auction_site.entity.BidderInfo;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.repository.BidRepository;
import com.auction.auction_site.repository.BidderInfoRepository;
import com.auction.auction_site.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class BidderInfoService {

    private final BidderInfoRepository bidderInfoRepository;
    private final BidRepository bidRepository;
    private final MemberRepository memberRepository;

    public BidderInfoDto createBidderInfo(BidderInfoDto dto, String loginId) {
        Bid bid = bidRepository.findById(dto.getBidId())
                .orElseThrow(() -> new IllegalArgumentException("Bid not found"));

        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            throw new IllegalStateException("유효하지 않은 인증 정보입니다.");
        }

        BidderInfo bidderInfo = new BidderInfo(
                dto.getName(),
                dto.getPhoneNumber(),
                dto.getRoadAddress(),
                dto.getDetailAddress(),
                bid,
                member
        );

        bidderInfo = bidderInfoRepository.save(bidderInfo);

        return convertToDto(bidderInfo);
    }

    // BidderInfo 조회
    public BidderInfoDto getBidderInfo(Long id) {
        BidderInfo bidderInfo = bidderInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BidderInfo not found"));
        return convertToDto(bidderInfo);
    }

    // BidderInfo를 BidderInfoDto로 변환하는 메서드
    private BidderInfoDto convertToDto(BidderInfo bidderInfo) {
        return BidderInfoDto.builder()
                .id(bidderInfo.getId())
                .name(bidderInfo.getName())
                .phoneNumber(bidderInfo.getPhoneNumber())
                .roadAddress(bidderInfo.getRoadAddress())
                .detailAddress(bidderInfo.getDetailAddress())
                .bidId(bidderInfo.getBid().getId())
                .memberId(bidderInfo.getMember().getId())  // memberId를 BidderInfo의 member에서 가져옴
                .build();
    }

}
