package com.jiangong.nmb.vo.grading;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 判分复核VO
 */
@Data
public class GradingReviewVO {
    private Long gradingResultId;
    private Long studentAnswerId;
    private String studentName;
    private String questionTitle;
    private String answerContent;
    private Double aiScore;
    private String aiComment;
    private String aiReason;
    private LocalDateTime aiGradingTime;
    private Boolean reviewed;
} 