package com.jiangong.nmb.vo.statistics;

import lombok.Data;

/**
 * 题目统计VO
 */
@Data
public class QuestionStatisticsVO {
    private Long questionId;
    private String questionTitle;
    private String questionType;
    private Double correctRate;
    private Integer totalAttempts;
    private String difficulty;
} 