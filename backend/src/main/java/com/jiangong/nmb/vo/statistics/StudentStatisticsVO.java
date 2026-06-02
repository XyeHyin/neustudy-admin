package com.jiangong.nmb.vo.statistics;

import lombok.Data;
import java.util.List;

/**
 * 学生统计VO
 */
@Data
public class StudentStatisticsVO {
    private Long studentId;
    private String studentName;
    private Integer totalPractices;
    private Double averageScore;
    private List<Double> scoreTrend;
} 