package com.jiangong.nmb.dto.practice;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 开始练习DTO
 */
@Data
public class PracticeStartDTO {
    @Schema(description = "试卷ID", example = "1", required = true)
    @NotNull(message = "试卷ID不能为空")
    private Long paperId;
} 