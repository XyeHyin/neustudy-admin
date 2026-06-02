package com.jiangong.nmb.vo.practice;

import lombok.Data;

/**
 * 答题详情VO
 */
@Data
public class PracticeAnswerVO {
    private Long questionId;
    private String questionTitle;
    private String questionType;
    private String answerContent;
    private Double score;
    private Boolean correct;
    private String aiComment;
    private Boolean marked;
} 