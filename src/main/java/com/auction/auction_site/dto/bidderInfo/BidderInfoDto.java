package com.auction.auction_site.dto.bidderInfo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BidderInfoDto {

    private Long id;
    private String name;
    private String phoneNumber;
    private String roadAddress;
    private String detailAddress;
    private Long bidId;
    private Long memberId;
}
