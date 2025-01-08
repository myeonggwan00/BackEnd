package com.auction.auction_site.utils;

import com.auction.auction_site.dto.ApiResponse;
import com.auction.auction_site.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 정상 응답 JSON, 에러 응답 JSON을 만들어주는 유틸리티 클래스
 */
public class Utility {
    public static void createSuccessResponse(HttpServletResponse response, String status, String message, Object data) throws IOException {
        ApiResponse apiResponse = new ApiResponse(status , message, data);
        String json = new ObjectMapper().writeValueAsString(apiResponse);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(json);
    }

    public static void createErrorResponse(HttpServletResponse response, String status, String code, String message) throws IOException {
        ErrorResponse apiResponse = new ErrorResponse(status , code, message);
        String json = new ObjectMapper().writeValueAsString(apiResponse);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(json);
    }
}
