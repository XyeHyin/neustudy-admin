package com.xyehyin.hexuanning.dto.practice;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
/**
 * 题目标记DTO
 */
@Data
public class PracticeMarkDTO {
    @Schema(description = "练习会话ID", example = "1", required = true)
    @NotNull(message = "练习会话ID不能为空")
    private Long practiceSessionId;
    @Schema(description = "题目ID", example = "101", required = true)
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    @Schema(description = "是否标记", example = "true", required = false)
    private Boolean marked;
} 