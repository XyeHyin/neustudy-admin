package com.xyehyin.hexuanning.vo.question;

import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.vo.knowledgepoint.KnowledgePointSimpleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目VO
 */
@Data
@Schema(description = "题目VO")
public class QuestionVO {

    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "题目类型")
    private Question.QuestionType type;

    @Schema(description = "题目类型描述")
    private String typeDescription;

    @Schema(description = "题目难度")
    private Question.Difficulty difficulty;

    @Schema(description = "题目难度描述")
    private String difficultyDescription;

    @Schema(description = "选项（JSON字符串）")
    private String options;

    @Schema(description = "正确答案")
    private String answer;

    @Schema(description = "答案解析")
    private String explanation;

    @Schema(description = "题目分值")
    private Integer score;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "是否为AI生成")
    private Boolean isAiGenerated;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "关联知识点")
    private KnowledgePointSimpleVO knowledgePoint;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}