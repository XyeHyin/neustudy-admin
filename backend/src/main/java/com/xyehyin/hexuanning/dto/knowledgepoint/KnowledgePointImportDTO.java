package com.xyehyin.hexuanning.dto.knowledgepoint;

import com.xyehyin.hexuanning.entity.KnowledgePoint;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class KnowledgePointImportDTO {
    private String name;
    private String description;
    private KnowledgePoint.Difficulty difficulty;
    private Integer orderNum;
    private String content;
    private String keywords;
    private Boolean enabled;
    
    // 添加原始难度值，用于错误报告
    private String originalDifficultyValue;
    
    // 添加警告信息
    private List<String> warnings = new ArrayList<>();
}