package com.jiangong.nmb.dto.question;

import com.jiangong.nmb.entity.Question;
import com.jiangong.nmb.entity.KnowledgePoint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建题目DTO（包含AI生成结果）
 */
@Data
@Schema(description = "创建题目DTO")
public class CreateQuestionDTO {

    @NotBlank(message = "题目标题不能为空")
    @Schema(description = "题目标题", example = "以下哪个是Java的基本数据类型？")
    private String title;

    @Schema(description = "题目内容", example = "请选择正确答案")
    private String content;

    @NotNull(message = "题目类型不能为空")
    @Schema(description = "题目类型")
    private Question.QuestionType type;

    @NotNull(message = "题目难度不能为空")
    @Schema(description = "题目难度")
    private KnowledgePoint.Difficulty difficulty;

    @Schema(description = "选项JSON字符串", example = "[{\"key\":\"A\",\"value\":\"int\"},{\"key\":\"B\",\"value\":\"String\"}]")
    private String options;

    @NotBlank(message = "答案不能为空")
    @Schema(description = "题目答案", example = "A")
    private String answer;

    @Schema(description = "答案解析", example = "int是Java的基本数据类型之一")
    private String explanation;

    @Schema(description = "题目分值", example = "1")
    private Integer score = 1;

    @Schema(description = "是否AI生成", example = "true")
    private Boolean isAiGenerated = true;

    @Schema(description = "标签", example = "基础,数据类型")
    private String tags;

    @NotNull(message = "知识点ID不能为空")
    @Schema(description = "知识点ID", example = "1")
    private Long knowledgePointId;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;
}