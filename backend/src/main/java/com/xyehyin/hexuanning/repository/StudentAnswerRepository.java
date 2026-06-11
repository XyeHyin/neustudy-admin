package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.StudentAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 学生答案数据访问接口
 */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    @Override
    @EntityGraph(attributePaths = {"practiceSession", "practiceSession.student", "question"})
    List<StudentAnswer> findAll();

    @Override
    @EntityGraph(attributePaths = {"practiceSession", "question"})
    Optional<StudentAnswer> findById(Long id);

    // 直接查询特定练习会话的所有答案
    @EntityGraph(attributePaths = {"practiceSession", "question"})
    List<StudentAnswer> findByPracticeSessionId(Long practiceSessionId);

    // 查询特定练习会话特定题目的答案
    @EntityGraph(attributePaths = {"practiceSession", "question"})
    Optional<StudentAnswer> findByPracticeSessionIdAndQuestionId(Long practiceSessionId, Long questionId);
}
