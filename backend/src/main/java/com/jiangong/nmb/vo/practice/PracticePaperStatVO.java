package com.jiangong.nmb.vo.practice;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PracticePaperStatVO {
    private Long paperId;
    private String paperTitle;
    private String paperDescription;
    private Integer timeLimit;
    private Integer totalScore;
    private Double minScore;
    private Double maxScore;
    private Double avgScore;
    private Integer totalAttempts;
    private Boolean hasFullScore;
    private String teacherName;
    private String status;
    private LocalDateTime createTime;
    private List<Double> scoreTrend;
} 