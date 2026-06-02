package com.jiangong.nmb.vo.practice;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 练习会话VO
 */
@Data
public class PracticeSessionVO {
    private Long id;
    private Long paperId;
    private String paperTitle;
    private Long studentId;
    private String studentName;
    private Integer attempt;
    private Boolean submitted;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private List<PracticeAnswerVO> answers;
} 