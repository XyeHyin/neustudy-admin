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
    @Query(value = "SELECT r FROM GradingResult r " +
           "JOIN FETCH r.studentAnswer a " +
           "JOIN FETCH a.question q " +
           "JOIN FETCH a.practiceSession ps " +
           "JOIN FETCH ps.paper p " +
           "JOIN FETCH ps.student s " +
           "WHERE r.finalScore IS NULL " +
           "AND q.type NOT IN (com.xyehyin.hexuanning.entity.Question.QuestionType.SINGLE_CHOICE, " +
           "com.xyehyin.hexuanning.entity.Question.QuestionType.MULTIPLE_CHOICE, " +
           "com.xyehyin.hexuanning.entity.Question.QuestionType.TRUE_FALSE) " +
           "AND (:paperId IS NULL OR p.id = :paperId) " +
           "AND (:questionTitle IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :questionTitle, '%')))",
           countQuery = "SELECT COUNT(r) FROM GradingResult r " +
           "JOIN r.studentAnswer a " +
           "JOIN a.question q " +
           "JOIN a.practiceSession ps " +
           "JOIN ps.paper p " +
           "WHERE r.finalScore IS NULL " +
           "AND q.type NOT IN (com.xyehyin.hexuanning.entity.Question.QuestionType.SINGLE_CHOICE, " +
           "com.xyehyin.hexuanning.entity.Question.QuestionType.MULTIPLE_CHOICE, " +
           "com.xyehyin.hexuanning.entity.Question.QuestionType.TRUE_FALSE) " +
           "AND (:paperId IS NULL OR p.id = :paperId) " +
           "AND (:questionTitle IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :questionTitle, '%')))")
    Page<GradingResult> findPendingReview(@Param("paperId") Long paperId,
                                          @Param("questionTitle") String questionTitle,
                                          Pageable pageable);

    /**
     * 分页查询已人工复核的判分结果，可选按试卷ID和题目名称关键词过滤
     */
    @Query(value = "SELECT r FROM GradingResult r " +
           "JOIN FETCH r.studentAnswer a " +
           "JOIN FETCH a.question q " +
           "JOIN FETCH a.practiceSession ps " +
           "JOIN FETCH ps.paper p " +
           "LEFT JOIN FETCH r.reviewTeacher rt " +
           "WHERE r.finalScore IS NOT NULL " +
           "AND (:paperId IS NULL OR p.id = :paperId) " +
           "AND (:questionTitle IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :questionTitle, '%')))",
           countQuery = "SELECT COUNT(r) FROM GradingResult r " +
           "JOIN r.studentAnswer a " +
           "JOIN a.question q " +
           "JOIN a.practiceSession ps " +
           "JOIN ps.paper p " +
           "WHERE r.finalScore IS NOT NULL " +
           "AND (:paperId IS NULL OR p.id = :paperId) " +
           "AND (:questionTitle IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :questionTitle, '%')))")
    Page<GradingResult> findReviewed(@Param("paperId") Long paperId,
                                     @Param("questionTitle") String questionTitle,
                                     Pageable pageable);
}
