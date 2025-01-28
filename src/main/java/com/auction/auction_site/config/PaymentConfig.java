package com.auction.auction_site.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.toss") // "payment.toss"로 시작하는 설정 값을 매핑
@Getter
@Setter
public class PaymentConfig {

 //   private String testClientApiKey;
    private String testSecretApiKey;
    private String successUrl;
    private String failUrl;
}
