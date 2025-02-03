package com.auction.auction_site.controller;

import com.auction.auction_site.exception.*;
import com.auction.auction_site.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class ExceptionController { // 예외 처리용 컨트롤러
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> illegalExHandle(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("fail", "BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("fail", "UNAUTHORIZED", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFound.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFound e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("fail", "NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("fail", "BAD_REQUEST", "유요하지 않은 토큰입니다."));
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredTokenException(ExpiredTokenException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("fail", "FORBIDDEN", "토큰이 만료되었습니다."));
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("fail", "BAD_REQUEST", "메일 전송에 실패했습니다."));
    }

    @ExceptionHandler(AlreadyParticipatedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyParticipatedException(AlreadyParticipatedException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("fail", "BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(AuctionFinishedException.class)
    public ResponseEntity<ErrorResponse> handleAuctionFinishedException(AuctionFinishedException e) {
        ErrorResponse errorResponse = null;
        String message = e.getMessage();

        if(message.contains("참여")) {
            errorResponse = new ErrorResponse("fail", "BAD_REQUEST", e.getMessage());
        } else if(message.contains("입찰")) {
            errorResponse = new ErrorResponse("fail", "BAD_REQUEST", e.getMessage());
        } else if(message.contains("취소")) {
            errorResponse = new ErrorResponse("fail", "BAD_REQUEST", e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}