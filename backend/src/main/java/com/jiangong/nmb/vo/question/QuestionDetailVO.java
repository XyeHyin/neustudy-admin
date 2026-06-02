package com.jiangong.nmb.vo.question;

import com.jiangong.nmb.entity.Question;
import com.jiangong.nmb.vo.knowledgepoint.KnowledgePointDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目详情VO
 */
@Data
@Schema(description = "题目详情VO")
public class QuestionDetailVO {

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

    @Schema(description = "选项")
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

    @Schema(description = "关联知识点详情")
    private KnowledgePointDetailVO knowledgePoint;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}