package com.xyehyin.hexuanning.vo.statistics;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatVO {
    private String key;
    private String label;
    private int value;
    private String unit;
    private double trend;
} 