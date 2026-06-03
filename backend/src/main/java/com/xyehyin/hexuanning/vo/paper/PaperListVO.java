package com.xyehyin.hexuanning.vo.paper;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 试卷列表VO
 */
@Data
public class PaperListVO {
    private Long id;
    private String title;
    private String teacherName;
    private String status;
    private Integer totalScore;
    private LocalDateTime createTime;
} 