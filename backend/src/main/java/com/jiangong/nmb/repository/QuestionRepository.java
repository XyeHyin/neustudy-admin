package com.jiangong.nmb.repository;

import com.jiangong.nmb.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目数据访问层
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 根据知识点ID查找题目
     */
    List<Question> findByKnowledgePointId(Long knowledgePointId);

    /**
     * 根据知识点ID和启用状态查找题目
     */
    List<Question> findByKnowledgePointIdAndEnabled(Long knowledgePointId, Boolean enabled);

    /**
     * 根据题目类型查找题目
     */
    List<Question> findByType(Question.QuestionType type);

    /**
     * 根据难度查找题目
     */
    List<Question> findByDifficulty(Question.Difficulty difficulty);

    /**
     * 统计知识点的题目数量
     */
    long countByKnowledgePointId(Long knowledgePointId);

    /**
     * 统计教师的题目数量（通过知识点->课程->教师关联）
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.knowledgePoint.course.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 分页查询所有题目（管理员用）
     */
    @Query("SELECT q FROM Question q WHERE " +
           "(:keyword IS NULL OR q.title LIKE %:keyword% OR q.content LIKE %:keyword%) AND " +
           "(:enabled IS NULL OR q.enabled = :enabled) AND " +
           "(:type IS NULL OR q.type = :type) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:knowledgePointId IS NULL OR q.knowledgePoint.id = :knowledgePointId)")
    Page<Question> findAllWithFilters(@Param("keyword") String keyword,
                                     @Param("enabled") Boolean enabled,
                                     @Param("type") Question.QuestionType type,
                                     @Param("difficulty") Question.Difficulty difficulty,
                                     @Param("knowledgePointId") Long knowledgePointId,
                                     Pageable pageable);

    /**
     * 分页查询教师的题目
     */
    @Query("SELECT q FROM Question q WHERE q.knowledgePoint.course.teacher.id = :teacherId AND " +
           "(:keyword IS NULL OR q.title LIKE %:keyword% OR q.content LIKE %:keyword%) AND " +
           "(:enabled IS NULL OR q.enabled = :enabled) AND " +
           "(:type IS NULL OR q.type = :type) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:knowledgePointId IS NULL OR q.knowledgePoint.id = :knowledgePointId)")
    Page<Question> findByTeacherIdWithFilters(@Param("teacherId") Long teacherId,
                                             @Param("keyword") String keyword,
                                             @Param("enabled") Boolean enabled,
                                             @Param("type") Question.QuestionType type,
                                             @Param("difficulty") Question.Difficulty difficulty,
                                             @Param("knowledgePointId") Long knowledgePointId,
                                             Pageable pageable);

    /**
     * 为试卷筛选题目（排除已有题目，按难度和分类筛选）
     */
    @Query("SELECT q FROM Question q WHERE " +
           "(:categoryId IS NULL OR q.knowledgePoint.course.category.id = :categoryId) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "q.enabled = true AND " +
           "(:excludeIds IS NULL OR q.id NOT IN :excludeIds)")
    List<Question> findQuestionsForPaper(@Param("categoryId") Long categoryId,
                                        @Param("difficulty") Question.Difficulty difficulty,
                                        @Param("excludeIds") List<Long> excludeIds,
                                        Pageable pageable);

    long countByCreateTimeBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}