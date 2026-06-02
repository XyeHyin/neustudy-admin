package com.jiangong.nmb.dto.paper;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 试卷题目DTO
 */
@Data
public class PaperQuestionDTO {
    @NotNull
    private Long questionId;
    private Integer orderNum;
    private Integer score;
} 