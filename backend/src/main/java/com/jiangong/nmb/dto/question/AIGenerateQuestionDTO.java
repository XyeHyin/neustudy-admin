package com.jiangong.nmb.dto.question;

import com.jiangong.nmb.entity.Question;
import com.jiangong.nmb.entity.KnowledgePoint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * AI生成题目请求DTO
 */
@Data
@Schema(description = "AI生成题目请求DTO")
public class AIGenerateQuestionDTO {

    @NotNull(message = "知识点ID不能为空")
    @Schema(description = "知识点ID", example = "1")
    private Long knowledgePointId;

    @Schema(description = "题目类型")
    private Question.QuestionType type;

    @Schema(description = "题目难度")
    private KnowledgePoint.Difficulty difficulty;

    @NotNull(message = "生成数量不能为空")
    @Positive(message = "生成数量必须大于0")
    @Schema(description = "生成题目数量", example = "5")
    private Integer count;

    @Schema(description = "额外需求", example = "请生成关于基础概念的题目")
    private String extraRequirement;
}