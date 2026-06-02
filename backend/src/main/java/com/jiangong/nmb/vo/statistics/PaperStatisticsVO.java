package com.jiangong.nmb.vo.statistics;

import lombok.Data;
import java.util.Map;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 试卷统计VO
 */
@Data
public class PaperStatisticsVO {
    @Schema(description = "试卷ID", example = "1")
    private Long paperId;
    @Schema(description = "试卷标题", example = "期中测试卷")
    private String paperTitle;
    @Schema(description = "总作答次数", example = "30")
    private Integer totalAttempts;
    @Schema(description = "平均分", example = "78.5")
    private Double averageScore;
    @Schema(description = "最高分", example = "100")
    private Double highestScore;
    @Schema(description = "最低分", example = "55")
    private Double lowestScore;
    @Schema(description = "平均正确率", example = "0.82")
    private Double correctRate;
    @Schema(description = "及格率", example = "0.7")
    private Double passRate; // 及格率
    @Schema(description = "成绩分布", example = "{\"90-100\":5,\"80-89\":8}")
    private Map<String, Integer> scoreDistribution; // 分数段统计，如{"90-100":5,"80-89":8}
    @Schema(description = "每题正确率列表")
    private List<QuestionCorrectRate> questionCorrectRates; // 每题正确率
    @Schema(description = "易错题列表")
    private List<WrongQuestion> wrongQuestions; // 易错题

    @Data
    public static class QuestionCorrectRate {
        @Schema(description = "题目ID", example = "101")
        private Long questionId;
        @Schema(description = "题目标题", example = "下列哪个是Java关键字？")
        private String questionTitle;
        @Schema(description = "正确率", example = "0.75")
        private Double correctRate;
    }
    @Data
    public static class WrongQuestion {
        @Schema(description = "题目ID", example = "102")
        private Long questionId;
        @Schema(description = "题目标题", example = "Java中String属于基本数据类型吗？")
        private String questionTitle;
        @Schema(description = "错误次数", example = "12")
        private Integer wrongCount;
    }
} 