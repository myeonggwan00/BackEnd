package com.auction.auction_site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = "com.auction.auction_site")
public class AuctionSiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuctionSiteApplication.class, args);
	}

}
