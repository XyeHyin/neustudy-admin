package com.xyehyin.hexuanning.vo.paper;

import lombok.Data;
import java.util.List;

/**
 * 试卷预览VO
 */
@Data
public class PaperPreviewVO {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimit;
    private List<PaperQuestionVO> questions;
} 