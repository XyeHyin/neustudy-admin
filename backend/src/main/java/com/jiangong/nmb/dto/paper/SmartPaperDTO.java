package com.jiangong.nmb.dto.paper;

import lombok.Data;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 智能组卷DTO
 */
@Data
public class SmartPaperDTO {
    @Schema(description = "难度", example = "EASY", required = false)
    private String difficulty;
    @Schema(description = "知识点ID列表", example = "[1,2,3]", required = false)
    private List<Long> knowledgePointIds;
    @Schema(description = "题型列表", example = "[\"SINGLE_CHOICE\",\"ESSAY\"]", required = false)
    private List<String> questionTypes;
    @Schema(description = "总题数", example = "10", required = false)
    private Integer totalQuestions;
    @Schema(description = "总分", example = "100", required = false)
    private Integer totalScore;
    /**
     * 题型分布策略，如{"SINGLE_CHOICE":5,"ESSAY":2}
     */
    @Schema(description = "题型分布策略", example = "{\"SINGLE_CHOICE\":5,\"ESSAY\":2}", required = false)
    private Map<String, Integer> questionTypeDistribution;
    @Schema(description = "时间限制", example = "60", required = false)
    private Integer timeLimit;
} 