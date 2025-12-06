package com.cloudread.Exception;

import com.cloudread.DTO.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<Map<String, Object>> handleAppException(AppException exception) {
        Map<String, String> errorsList = new HashMap<>();
        exception.getErrorList().forEach(error -> errorsList.put(String.valueOf(error.getCode()), error.getMessage()));

        Map<String, Object> response = Map.of(
                "timestamp", LocalDateTime.now(),
                "errors", errorsList
        );

        HttpStatus responseStatus = exception.getHttpStatus();

        return ResponseEntity.status(responseStatus).body(response);
    }

    @ExceptionHandler(value = IOException.class)
    ResponseEntity<ApiResponse<Object>> handleIOException(IOException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errorList = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errorList.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorList);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(value = NullPointerException.class)
    ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException exception) {
        ApiResponse<Object> response = ApiResponse.builder()
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
