package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.GradingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 判分结果数据访问接口
 */
@Repository
public interface GradingResultRepository extends JpaRepository<GradingResult, Long> {
    // 可根据需要添加自定义查询方法
    /**
     * 分页查询所有待人工复核的判分结果，可选按试卷ID和题目名称关键词过滤
     */
    @Query("SELECT r FROM GradingResult r WHERE r.finalScore IS NULL " +
           "AND r.studentAnswer.question.type NOT IN (com.xyehyin.hexuanning.entity.Question.QuestionType.SINGLE_CHOICE, " +
           "com.xyehyin.hexuanning.entity.Question.QuestionType.MULTIPLE_CHOICE, " +
           "com.xyehyin.hexuanning.entity.Question.QuestionType.TRUE_FALSE) " +
           "AND (:paperId IS NULL OR r.studentAnswer.practiceSession.paper.id = :paperId) " +
           "AND (:questionTitle IS NULL OR LOWER(r.studentAnswer.question.title) LIKE LOWER(CONCAT('%', :questionTitle, '%')))" )
    Page<GradingResult> findPendingReview(@Param("paperId") Long paperId,
                                          @Param("questionTitle") String questionTitle,
                                          Pageable pageable);

    /**
     * 分页查询已人工复核的判分结果，可选按试卷ID和题目名称关键词过滤
     */
    @Query("SELECT r FROM GradingResult r WHERE r.finalScore IS NOT NULL " +
           "AND (:paperId IS NULL OR r.studentAnswer.practiceSession.paper.id = :paperId) " +
           "AND (:questionTitle IS NULL OR LOWER(r.studentAnswer.question.title) LIKE LOWER(CONCAT('%', :questionTitle, '%')))" )
    Page<GradingResult> findReviewed(@Param("paperId") Long paperId,
                                     @Param("questionTitle") String questionTitle,
                                     Pageable pageable);
} 