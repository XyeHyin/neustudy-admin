package com.xyehyin.hexuanning.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量创建题目DTO
 */
@Data
@Schema(description = "批量创建题目DTO")
public class BatchCreateQuestionDTO {

    @NotNull(message = "题目列表不能为空")
    @Size(min = 1, message = "至少需要一道题目")
    @Valid
    @Schema(description = "题目列表")
    private List<CreateQuestionDTO> questions;

    @Schema(description = "是否为AI生成", example = "true")
    private Boolean isAiGenerated = false;
}