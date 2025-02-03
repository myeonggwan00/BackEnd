package com.auction.auction_site.service;

import com.auction.auction_site.dto.auction.AuctionDetailsDto;
import com.auction.auction_site.dto.auction.RequestBidDto;
import com.auction.auction_site.dto.auction.ResponseBidDto;
import com.auction.auction_site.entity.*;
import com.auction.auction_site.exception.AlreadyParticipatedException;
import com.auction.auction_site.exception.AuctionFinishedException;
import com.auction.auction_site.exception.EntityNotFound;
import com.auction.auction_site.repository.AuctionParticipantRepository;
import com.auction.auction_site.repository.AuctionRepository;
import com.auction.auction_site.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.auction.auction_site.entity.PaymentStatus.PENDING;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final AuctionParticipantRepository auctionParticipantRepository;
    private final BidRepository bidRepository;

    public AuctionParticipant participateAuction(Member member, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        if(auction.getAuctionStatus().equals(AuctionStatus.FINISHED.getLabel())) {
            throw new AuctionFinishedException("해당 경매는 종료되어서 참여할 수 없습니다.");
        }

        // 경매 참여자 관리
        AuctionParticipant participant = auctionParticipantRepository.findByAuctionIdAndMemberId(auction.getId(), member.getId());

        if(participant != null) {
            throw new AlreadyParticipatedException("이미 해당 경매에 참여중입니다.");
        }

        return AuctionParticipant.participant(member, auction);
    }

    public AuctionDetailsDto auctionBid(Member member, Long auctionId, RequestBidDto requestBidDto) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        // 경매 참여자
        AuctionParticipant participant = auctionParticipantRepository.findByAuctionIdAndMemberId(auctionId, member.getId());

        if(participant == null) {
            throw new EntityNotFound("해당 경매에 참여하지 않았습니다.");
        }

        if(auction.getAuctionStatus().equals(AuctionStatus.FINISHED.getLabel())) {
            throw new AuctionFinishedException("해당 경매는 종료되어서 입찰할 수 없습니다.");
        }

        // 입찰시 입찰 금액이 해당 경매의 최대 가격보다 크면 변경
        auction.checkCurrentMaxPrice(requestBidDto.getBidPrice());

        // 입찰 정보 추가
        Bid newBid = Bid.addBid(auction, participant, requestBidDto.getBidPrice());

        bidRepository.save(newBid);

        List<ResponseBidDto> bids = new ArrayList<>();

        bidRepository.findByAuctionId(auctionId).forEach(bid -> {
            bids.add(bid.fromBid());
        });

        return AuctionDetailsDto.builder()
                .auctionStatus(auction.getAuctionStatus())  // 현재 사용자의 경매 승리자 여부
                .auctionParticipantStatus(participant.getAuctionParticipantStatus())
                .auctionParticipants(auction.getAuctionParticipantCount())
                .remainingAuctionTime(calculateTimeDifference(auction.getEndDate()))
                .bids(bids)
                .build();
    }

    public AuctionDetailsDto getBidsByAuction(Member member, Long auctionId) {
        // 현재 사용자의 경매 승리자 여부
        String isAuctionWinner = auctionParticipantRepository.findByMemberId(member.getId()).getAuctionParticipantStatus();

        List<ResponseBidDto> bids = new ArrayList<>();

        bidRepository.findByAuctionId(auctionId).forEach(bid -> {
            bids.add(bid.fromBid());
        });

        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        return AuctionDetailsDto.builder()
                .auctionStatus(auction.getAuctionStatus())  // 현재 사용자의 경매 승리자 여부
                .auctionParticipantStatus(isAuctionWinner)
                .auctionParticipants(auction.getAuctionParticipantCount())
                .remainingAuctionTime(calculateTimeDifference(auction.getEndDate()))
                .bids(bids)
                .build();
    }

    public void cancelBids(Member member, Long auctionId) {
        AuctionParticipant participant = auctionParticipantRepository.findByMemberId(member.getId());
        List<Bid> bids = bidRepository.findByAuctionParticipantId(participant.getId());
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        if(auction.getAuctionStatus().equals(AuctionStatus.FINISHED.getLabel())) {
            throw new AuctionFinishedException("해당 경매는 종료되어서 취소할 수 없습니다.");
        }

        // 가장 최근 입찰 찾기 (입찰 날짜 기준 내림차순 정렬)
        Bid bid = bids.stream()
                .max(Comparator.comparing(Bid::getBidDate))
                .orElseThrow(() -> new EntityNotFound("해당 경매에 입찰한 기록이 없습니다."));

        // 입찰 취소 (삭제)
        bidRepository.delete(bid);

        // 최고 입찰가격 갱신
        auction.updateCurrentMaxPrice(bidRepository.findMaxBidAmount(auctionId));
    }


    /**
     * 남은 경매 시간 구하는 메서드 - 공부 필요
     */
    public static String calculateTimeDifference(LocalDateTime endTime) {
        Duration duration = Duration.between(LocalDateTime.now(), endTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        // 1일 이상 남은 경우: "X일 X시간 X분"
        if (days > 0) {
            return String.format("%d일 %02d시간 %02d분", days, hours, minutes);
        } else {
            // 1일 미만 남은 경우: "X시간 X분"
            return String.format("%02d시간 %02d분", hours, minutes);
        }
    }

    /**
     * 1분 동안 해당 스케줄 함수(경매 종료 여부 확인 및 종료시 최종 낙찰자 설정) 실행 - 공부 필요
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void closeExpiredAuctions() {
        List<Auction> expiredAuctions = auctionRepository.findByEndDateBeforeAndAuctionStatus(LocalDateTime.now(), AuctionStatus.RUNNING.getLabel());

        for (Auction auction : expiredAuctions) {
            auction.changeAuctionStatus(AuctionStatus.FINISHED);
            auction.determineFinalWinner();
        }

        AuctionParticipant auctionParticipant = auctionParticipantRepository.findByPaymentDeadlineBeforeAndPaymentStatus(LocalDateTime.now(), PENDING.getLabel());

        if(auctionParticipant != null) {
            auctionParticipant.processExpiredPayment();
        }
    }
}
