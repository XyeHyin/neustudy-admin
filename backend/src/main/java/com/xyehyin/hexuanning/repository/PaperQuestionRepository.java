package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.PaperQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 试卷题目关联数据访问接口
 */
@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, Long> {
    // 可根据需要添加自定义查询方法
    List<PaperQuestion> findByPaperId(Long paperId);
    Optional<PaperQuestion> findByPaperIdAndQuestionId(Long paperId, Long questionId);
} 