package com.jiangong.nmb.dto.grading;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 人工复核DTO
 */
@Data
public class ManualGradingDTO {
    @Schema(description = "判分结果ID", example = "2001", required = true)
    @NotNull(message = "判分结果ID不能为空")
    private Long gradingResultId;
    @Schema(description = "最终得分", example = "9.0", required = true)
    @NotNull(message = "最终得分不能为空")
    private Double finalScore;
    @Schema(description = "教师评语", example = "表达较好，注意细节", required = false)
    private String teacherComment;
} 