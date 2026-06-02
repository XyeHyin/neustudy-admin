package com.jiangong.nmb.vo.paper;

import lombok.Data;

/**
 * 试卷统计VO
 */
@Data
public class PaperStatisticsVO {
    private Long paperId;
    private Integer totalAttempts;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Double correctRate;
} 