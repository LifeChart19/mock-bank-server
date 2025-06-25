package com.example.mockbank.common.exception;

import com.example.mockbank.common.enums.ErrorCode;
import com.example.mockbank.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onError(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_AMOUNT; // 혹은 별도 Validation 에러코드
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onError(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED; // 혹은 공통 에러코드
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onError(errorCode));
    }
}
