package com.auction.auction_site.entity;

public enum PaymentStatus {
    NO_PAYMENT("결제 불가"),   // 결제할 수 없는 상태
    PENDING("결제 대기"),      // 결제 대기
    COMPLETED("결제 완료"),    // 결제 완료
    EXPIRED("결제 기간 만료");       // 24시간 내에 결제 하지 않음

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
