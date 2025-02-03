package com.auction.auction_site.entity;

public enum AuctionStatus {
    RUNNING("경매 진행중"), FINISHED("경매 종료");

    private final String label;

    AuctionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
