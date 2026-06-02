package com.jiangong.nmb.vo.paper;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 试卷题目VO
 */
@Data
public class PaperQuestionVO {
    @Schema(description = "题目ID", example = "101")
    private Long questionId;
    @Schema(description = "题目标题", example = "下列哪个是Java关键字？")
    private String title;
    @Schema(description = "题目类型", example = "SINGLE_CHOICE")
    private String type;
    @Schema(description = "题目顺序号", example = "1")
    private Integer orderNum;
    @Schema(description = "题目分值", example = "5")
    private Integer score;
} 