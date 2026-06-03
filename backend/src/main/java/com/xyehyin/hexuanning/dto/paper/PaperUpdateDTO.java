package com.xyehyin.hexuanning.dto.paper;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 试卷更新DTO
 */
@Data
public class PaperUpdateDTO {
    private String title;
    private String description;
    private Integer timeLimit;
    private Boolean showAnswer;
    private Boolean allowRetry;
    private String questionOrderType;
    private String status;
} 