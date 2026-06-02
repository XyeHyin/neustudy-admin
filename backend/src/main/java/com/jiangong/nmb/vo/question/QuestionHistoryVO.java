package com.jiangong.nmb.vo.question;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目历史版本VO
 */
@Data
public class QuestionHistoryVO {
    /** 版本号 */
    private Number revision;
    /** 版本创建时间 */
    private LocalDateTime revisionDate;
    /** 题目快照 */
    private QuestionDetailVO question;
} 