package com.auction.auction_site.controller;

import com.auction.auction_site.exception.EntityNotFound;
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
}