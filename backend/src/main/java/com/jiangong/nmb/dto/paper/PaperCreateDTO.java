package com.jiangong.nmb.dto.paper;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 试卷创建DTO
 */
@Data
public class PaperCreateDTO {
    @Schema(description = "试卷标题", example = "期中测试卷", required = true)
    @NotBlank
    private String title;
    @Schema(description = "试卷描述", example = "2024年春季学期期中考试", required = false)
    private String description;
    @Schema(description = "教师ID", example = "1", required = true)
    @NotNull
    private Long teacherId;
    @Schema(description = "时间限制（分钟）", example = "90", required = false)
    private Integer timeLimit;
    private Boolean showAnswer;
    private Boolean allowRetry;
    private String questionOrderType; // FIXED or RANDOM
} 