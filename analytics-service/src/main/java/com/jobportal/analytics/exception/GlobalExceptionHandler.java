package com.jobportal.analytics.exception;

import com.jobportal.common.dto.ErrorResponse;
import com.jobportal.common.exception.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.builder().status(403).error("Forbidden")
                .message(ex.getMessage()).timestamp(LocalDateTime.now()).build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder().status(500).error("Internal Server Error")
                .message(ex.getMessage()).timestamp(LocalDateTime.now()).build());
    }
}
