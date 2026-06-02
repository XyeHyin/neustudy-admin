package com.jiangong.nmb.vo.paper;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 试卷详情VO
 */
@Data
public class PaperDetailVO {
    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Integer timeLimit;
    private Boolean showAnswer;
    private Boolean allowRetry;
    private String questionOrderType;
    private String status;
    private Integer totalScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PaperQuestionVO> questions;
} 