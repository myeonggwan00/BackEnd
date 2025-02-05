package com.auction.auction_site.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String paymentKey;
    private String orderId;
    private String amount;
    private Long productId;


}
