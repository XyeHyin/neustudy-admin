package com.xyehyin.hexuanning.dto.grading;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * AI判分DTO
 */
@Data
public class AIGradingDTO {
    @Schema(description = "学生答案ID", example = "1001", required = true)
    @NotNull(message = "学生答案ID不能为空")
    private Long studentAnswerId;
    @Schema(description = "AI评分", example = "8.5", required = false)
    private Double aiScore;
    @Schema(description = "AI评语", example = "答案思路清晰，表达完整", required = false)
    private String aiComment;
    @Schema(description = "AI判分理由", example = "覆盖了主要知识点", required = false)
    private String aiReason;
} 