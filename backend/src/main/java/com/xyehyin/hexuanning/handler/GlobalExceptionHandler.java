package com.xyehyin.hexuanning.handler;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.HTTP_FORBIDDEN).body(ApiResponse.error(HttpStatus.HTTP_FORBIDDEN, "权限不足"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("未处理的服务端异常", e);
        return ResponseEntity.status(HttpStatus.HTTP_INTERNAL_ERROR)
                .body(ApiResponse.error(HttpStatus.HTTP_INTERNAL_ERROR, "服务器内部错误"));
    }
}
