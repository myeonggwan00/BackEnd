package com.auction.auction_site.entity;

public enum AuctionParticipantStatus {
    WINNER("승리자"), PENDING("보류");

    private final String label;

    AuctionParticipantStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
