package com.xyehyin.hexuanning.vo.practice;

import lombok.Data;
import java.util.List;

/**
 * 练习概览VO
 */
@Data
public class PracticeOverviewVO {
    private Long practiceSessionId;
    private Long paperId;
    private String paperTitle;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer markedQuestions;
    private Boolean submitted;
    private List<QuestionProgress> questionProgressList;

    @Data
    public static class QuestionProgress {
        private Long questionId;
        private String questionTitle;
        private Boolean answered;
        private Boolean marked;
    }
} 