package com.xyehyin.hexuanning.dto.paper;

import lombok.Data;

/**
 * 试卷查询DTO
 */
@Data
public class PaperQueryDTO {
    private String title;
    private Long teacherId;
    private String status;
    private Integer page = 1;
    private Integer size = 10;
} 