package com.auction.auction_site.service;

import com.auction.auction_site.entity.Auction;
import com.auction.auction_site.entity.AuctionParticipant;
import com.auction.auction_site.entity.AuctionStatus;
import com.auction.auction_site.repository.AuctionParticipantRepository;
import com.auction.auction_site.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.auction.auction_site.entity.PaymentStatus.PENDING;

@Component
@RequiredArgsConstructor
@Transactional
public class AuctionScheduler {
    private final AuctionRepository auctionRepository;
    private final AuctionParticipantRepository auctionParticipantRepository;

    /**
     * 경매 종료 처리
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void closeExpiredAuctions() {
        // 경매 종료 기간이 지난 경매 조회
        List<Auction> expiredAuctions = auctionRepository.findByEndDateBeforeAndAuctionStatus(LocalDateTime.now(), AuctionStatus.RUNNING.getLabel());

        for (Auction auction : expiredAuctions) {
            auction.changeAuctionStatus(AuctionStatus.FINISHED); // 경매 상태 종료로 변경
            auction.determineFinalWinner(); // 경매 승리자 결정
        }
    }

    /**
     * 결제 미이행 처리
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void processExpiredPayments() {
        // 결제 기간 내에 결제하지 않은 경매 참여자 조회
        List<AuctionParticipant> auctionParticipant = auctionParticipantRepository.findByPaymentDeadlineBeforeAndPaymentStatus(LocalDateTime.now(), PENDING.getLabel());

        if(auctionParticipant == null) { return; }

        for (AuctionParticipant participant : auctionParticipant) {
            participant.processExpiredPayment();
        }
    }
}
