package com.auction.auction_site.utils;

import com.auction.auction_site.dto.ErrorResponse;
import com.auction.auction_site.dto.SuccessResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 정상 응답 JSON, 에러 응답 JSON을 만들어주는 유틸리티 클래스
 */
public class Utility {
    public static void sendSuccessJsonResponse(HttpServletResponse response, String message, Object data) throws IOException {
        String json = new ObjectMapper().writeValueAsString(SuccessResponse.success(message, data));

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(json);
    }

    public static void sendErrorJsonResponse(HttpServletResponse response, String code, String message) throws IOException {
        String json = new ObjectMapper().writeValueAsString(ErrorResponse.error(code, message));

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(json);
    }
}
