package com.xyehyin.hexuanning.dto.practice;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 练习查询DTO
 */
@Data
public class PracticeQueryDTO {
    @Schema(description = "试卷ID", example = "1", required = false)
    private Long paperId;
    @Schema(description = "学生ID", example = "10001", required = false)
    private Long studentId;
    @Schema(description = "是否已提交", example = "true", required = false)
    private Boolean submitted;
    private Integer page = 1;
    private Integer size = 10;
} 