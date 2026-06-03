package com.xyehyin.hexuanning.vo.grading;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 判分结果VO
 */
@Data
public class GradingResultVO {
    private Long gradingResultId;
    private Long studentAnswerId;
    private Double aiScore;
    private String aiComment;
    private String aiReason;
    private LocalDateTime aiGradingTime;
    private Double finalScore;
    private String teacherComment;
    private String reviewTeacherName;
    private LocalDateTime reviewTime;
} 