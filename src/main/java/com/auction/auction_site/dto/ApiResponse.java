package com.auction.auction_site.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // NULL 필드는 제외
public class ApiResponse {
    private String status;
    private String Message;
    private Object data;

    public ApiResponse() {}
}