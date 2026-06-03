package com.xyehyin.hexuanning.vo.practice;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习详情VO
 */
@Data
public class PracticeDetailVO {
    @Schema(description = "练习会话ID", example = "123")
    private Long practiceSessionId;
    
    @Schema(description = "试卷ID", example = "456")
    private Long paperId;
    
    @Schema(description = "试卷标题", example = "Java基础知识测试")
    private String paperTitle;
    
    @Schema(description = "试卷描述", example = "本试卷测试Java基础知识掌握情况")
    private String paperDescription;
    
    @Schema(description = "时间限制(分钟)", example = "60")
    private Integer timeLimit;
    
    @Schema(description = "总分", example = "100")
    private Integer totalScore;
    
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    
    @Schema(description = "是否已提交", example = "false")
    private Boolean submitted;
    
    @Schema(description = "题目列表")
    private List<PracticeQuestionVO> questions;
} 