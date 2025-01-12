package com.auction.auction_site.dto;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String status;
    private String code;
    private String message;

    public static ErrorResponse error(String code, String message) {
        return ErrorResponse.builder()
                .status("fail")
                .code(code)
                .message(message)
                .build();
    }
}
