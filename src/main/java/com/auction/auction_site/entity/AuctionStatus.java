package com.auction.auction_site.entity;

public enum AuctionStatus {
    RUNNING("경매 진행중"),
    FINISHED("경매 종료"),
    COMPLETED("경매 완료"),
    CANCELED("경매 취소"),
    DELETED("경매 삭제");

    private final String label;

    AuctionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
