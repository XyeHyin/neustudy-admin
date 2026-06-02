package com.jiangong.nmb.dto.grading;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 判分请求DTO
 */
@Data
public class GradingRequestDTO {
    @Schema(description = "学生答案ID", example = "1001", required = true)
    @NotNull(message = "学生答案ID不能为空")
    private Long studentAnswerId;
    @Schema(description = "学生答案内容", example = "public static void main(String[] args) {}", required = false)
    private String answerContent;
} 