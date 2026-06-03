package com.xyehyin.hexuanning.dto.practice;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 提交答案DTO
 */
@Data
public class PracticeSubmitDTO {
    @Schema(description = "练习会话ID", example = "1", required = true)
    @NotNull
    private Long practiceSessionId;
    @Schema(description = "答案列表", required = true)
    @NotNull
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        @Schema(description = "题目ID", example = "101", required = true)
        @NotNull(message = "题目ID不能为空")
        private Long questionId;
        @Schema(description = "答案内容", example = "A", required = false)
        @Size(max = 2000, message = "答案内容过长")
        private String answerContent;
        @Schema(description = "是否标记", example = "true", required = false)
        private Boolean marked;
    }
} 