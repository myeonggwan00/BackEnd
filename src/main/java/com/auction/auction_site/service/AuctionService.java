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

    /**
     * 경매 참여
     */
    public AuctionParticipant participateAuction(Member member, Long auctionId) {
        // 참여하려는 경매 정보 가져오기
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        // 참여하려는 경매가 종료된 상태인 경우
        if(auction.getAuctionStatus().equals(AuctionStatus.FINISHED.getLabel())) {
            throw new AuctionFinishedException("해당 경매는 종료되어서 참여할 수 없습니다.");
        }

        if(auctionParticipantRepository.existsByAuctionIdAndMemberId(auction.getId(), member.getId())) {
            throw new AlreadyParticipatedException("이미 해당 경매에 참여중입니다.");
        }

        return AuctionParticipant.participant(member, auction);
    }

    /**
     * 경매 입찰
     */
    public AuctionDetailsDto auctionBid(Member member, Long auctionId, RequestBidDto requestBidDto) {
        // 참여하려는 경매 정보 가져오기
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        // 해당 회원이 해당 경매에 참여했는지 확인
        AuctionParticipant participant = auctionParticipantRepository
                .findByAuctionIdAndMemberId(auctionId, member.getId())
                .orElseThrow(() -> new EntityNotFound("해당 경매에 참여하지 않았습니다."));

        if(auction.getAuctionStatus().equals(AuctionStatus.FINISHED.getLabel())) {
            throw new AuctionFinishedException("해당 경매는 종료되어서 입찰할 수 없습니다.");
        }

        // 입찰시 입찰 금액이 해당 경매의 최대 입찰가보다 큰지 확인
        auction.checkCurrentMaxPrice(requestBidDto.getBidPrice());

        // 입찰 정보 저장
        Bid newBid = Bid.addBid(auction, participant, requestBidDto.getBidPrice());

        bidRepository.save(newBid);

        // 입찰 내역
        List<ResponseBidDto> bids = new ArrayList<>();

        // 해당 경매에 입찰 정보를 입찰 내역에 추가
        bidRepository.findByAuctionId(auctionId).forEach(bid -> {
            bids.add(bid.fromBid());
        });

        return AuctionDetailsDto.builder()
                .auctionStatus(auction.getAuctionStatus()) // 경매 상태
                .auctionParticipantStatus(participant.getAuctionParticipantStatus()) // 경매 참여자의 상태
                .auctionParticipants(auction.getAuctionParticipantCount()) // 경매 참여자 수
                .remainingAuctionTime(calculateTimeDifference(auction.getEndDate())) // 경매 종료까지 남은 시간
                .bids(bids) // 입찰 내역
                .build();
    }

    /**
     * 입찰 내역 조회
     */
    public AuctionDetailsDto getBidsByAuction(Member member, Long auctionId) {
        // 경매 정보 가져오기
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        // 경매 참여자의 상태(경매에 참여하지 않은 사람은 null)
        String auctionParticipantStatus = auctionParticipantRepository.findAuctionParticipantStatusByMemberId(member.getId(), auctionId);

        // 입찰 내역
        List<ResponseBidDto> bids = new ArrayList<>();

        // 해당 경매에 입찰 정보를 입찰 내역에 추가
        bidRepository.findByAuctionId(auctionId).forEach(bid -> {
            bids.add(bid.fromBid());
        });

        return AuctionDetailsDto.builder()
                .auctionStatus(auction.getAuctionStatus()) // 경매 상태
                .auctionParticipantStatus(auctionParticipantStatus) // 현재 회원의 경매 승리자 여부
                .auctionParticipants(auction.getAuctionParticipantCount()) // 경매 참여자 수
                .remainingAuctionTime(calculateTimeDifference(auction.getEndDate())) // 경매 종료까지 남은 시간
                .bids(bids) // 입찰 내역
                .build();
    }

    /**
     *  입찰 취소
     */
    public void cancelBids(Member member, Long auctionId) {
        // 경매 정보 가져오기
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new EntityNotFound("해당 경매는 존재하지 않습니다."));

        // 해당 경매에 참여한 참여자 조회 후 입찰한 내역 조회
        AuctionParticipant participant = auctionParticipantRepository.findByAuctionIdAndMemberId(auctionId, member.getId())
                .orElseThrow(() -> new EntityNotFound("해당 경매에 참여하지 않았습니다."));

        List<Bid> bids = bidRepository.findByAuctionParticipantId(participant.getId());

        // 입찰 취소하려는 경매가 종료된 경우
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
     * 남은 경매 시간 구하기
     */
    public static String calculateTimeDifference(LocalDateTime endTime) {
        Duration duration = Duration.between(LocalDateTime.now(), endTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) { // 1일 이상 남은 경우: "X일 X시간 X분"
            return String.format("%d일 %02d시간 %02d분", days, hours, minutes);
        } else { // 1일 미만 남은 경우: "X시간 X분"
            return String.format("%02d시간 %02d분", hours, minutes);
        }
    }

    /**
     * 1분마다 해당 스케줄 함수(경매 종료 여부 확인 및 종료시 최종 낙찰자 설정) 실행
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void closeExpiredAuctions() {
        // 경매 종료 기간이 지난 경매 조회
        List<Auction> expiredAuctions = auctionRepository.findByEndDateBeforeAndAuctionStatus(LocalDateTime.now(), AuctionStatus.RUNNING.getLabel());

        for (Auction auction : expiredAuctions) {
            auction.changeAuctionStatus(AuctionStatus.FINISHED); // 경매 상태 종료로 변경
            auction.determineFinalWinner(); // 경매 승리자 결정
        }

        // 결제 기간 내에 결제하지 않은 경매 참여자 조회
        List<AuctionParticipant> auctionParticipant = auctionParticipantRepository.findByPaymentDeadlineBeforeAndPaymentStatus(LocalDateTime.now(), PENDING.getLabel());

        if(auctionParticipant == null) { return; }

        for (AuctionParticipant participant : auctionParticipant) {
            participant.processExpiredPayment();
        }
    }
}
