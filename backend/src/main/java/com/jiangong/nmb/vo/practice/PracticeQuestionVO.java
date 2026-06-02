package com.jiangong.nmb.vo.practice;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 练习题目详情VO
 */
@Data
public class PracticeQuestionVO {
    @Schema(description = "题目ID", example = "101")
    private Long questionId;
    
    @Schema(description = "题目标题", example = "下列哪个是Java关键字？")
    private String title;
    
    @Schema(description = "题目内容", example = "请选择以下选项中的Java关键字")
    private String content;
    
    @Schema(description = "题目类型", example = "SINGLE_CHOICE")
    private String type;
    
    @Schema(description = "题目顺序号", example = "1")
    private Integer orderNum;
    
    @Schema(description = "题目分值", example = "5")
    private Integer score;
    
    @Schema(description = "题目选项", example = "[{\"key\":\"A\",\"value\":\"finally\"},{\"key\":\"B\",\"value\":\"finalize\"}]")
    private String options;
    
    @Schema(description = "题目难度", example = "MEDIUM")
    private String difficulty;
    
    @Schema(description = "学生答案", example = "A")
    private String studentAnswer;
    
    @Schema(description = "是否已标记", example = "true")
    private Boolean marked;
} 