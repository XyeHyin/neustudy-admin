package com.xyehyin.hexuanning.handler;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice()
public class GlobalExceptionHandler {
    @ExceptionHandler(StatefulException.class)
    public ResponseEntity<ApiResponse<?>> handleStatefulException(StatefulException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.error(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String errorMsg = fieldError.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.HTTP_BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.HTTP_BAD_REQUEST, errorMsg));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.HTTP_ENTITY_TOO_LARGE).body(ApiResponse.error(HttpStatus.HTTP_ENTITY_TOO_LARGE, "上传文件大小超过限制"));
    }
}