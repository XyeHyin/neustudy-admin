package com.jiangong.nmb.vo.practice;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习记录VO
 */
@Data
public class PracticeRecordVO {
    private Long practiceSessionId;
    private Long paperId;
    private String paperTitle;
    private Double totalScore;
    private Double correctRate;
    private Boolean submitted;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
} 