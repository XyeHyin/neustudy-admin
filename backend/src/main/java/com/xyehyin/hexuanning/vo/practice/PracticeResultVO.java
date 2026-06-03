package com.xyehyin.hexuanning.vo.practice;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 练习结果VO
 */
@Data
public class PracticeResultVO {
    private Long practiceSessionId;
    private Long paperId;
    private String paperTitle;
    private Double totalScore;
    private Double correctRate;
    private LocalDateTime submitTime;
    private List<PracticeAnswerVO> answers;
} 