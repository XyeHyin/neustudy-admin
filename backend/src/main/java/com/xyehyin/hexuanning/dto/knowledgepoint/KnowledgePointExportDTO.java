package com.xyehyin.hexuanning.dto.knowledgepoint;

import lombok.Data;

@Data
public class KnowledgePointExportDTO {
    private Long id;
    private Long courseId;
    private String name;
    private String description;
    private String courseName;
    private String difficulty;
    private Integer orderNum;
    private String content;
    private String keywords;
    private String enabled;
    private String createTime;
    private String updateTime;
    private Long questionCount;
}
