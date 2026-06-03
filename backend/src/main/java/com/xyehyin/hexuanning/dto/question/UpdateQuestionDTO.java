package com.xyehyin.hexuanning.dto.question;

import com.xyehyin.hexuanning.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新题目DTO
 */
@Data
@Schema(description = "更新题目DTO")
public class UpdateQuestionDTO {

    @NotBlank(message = "题目标题不能为空")
    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容")
    private String content;

    @NotNull(message = "题目类型不能为空")
    @Schema(description = "题目类型")
    private Question.QuestionType type;

    @NotNull(message = "题目难度不能为空")
    @Schema(description = "题目难度")
    private Question.Difficulty difficulty;

    @Schema(description = "选项（JSON字符串格式）")
    private String options;

    @NotBlank(message = "正确答案不能为空")
    @Schema(description = "正确答案")
    private String answer;

    @Schema(description = "答案解析")
    private String explanation;

    @Schema(description = "题目分值", example = "2")
    private Integer score;

    @NotNull(message = "知识点ID不能为空")
    @Schema(description = "关联知识点ID", example = "1")
    private Long knowledgePointId;

    @Schema(description = "是否为AI生成", example = "false")
    private Boolean isAiGenerated;

    @Schema(description = "标签", example = "基础,语法")
    private String tags;

    @Schema(description = "是否启用")
    private Boolean enabled;
}