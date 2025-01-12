package com.auction.auction_site.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // NULL 필드는 제외
public class SuccessResponse {
    private String status;
    private String message;
    private Object data;

    public SuccessResponse() {}

    public static SuccessResponse success(String message, Object data) {
        return SuccessResponse.builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }
}