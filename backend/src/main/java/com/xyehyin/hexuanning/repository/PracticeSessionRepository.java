package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 练习会话数据访问接口
 */
@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
    // 查询特定学生的所有练习记录
    List<PracticeSession> findByStudentId(Long studentId);
    
    // 查询特定试卷的所有练习记录
    List<PracticeSession> findByPaperId(Long paperId);
    
    // 查询特定学生特定试卷的所有练习记录
    List<PracticeSession> findByStudentIdAndPaperId(Long studentId, Long paperId);
    
    // 查询特定学生特定试卷指定提交状态的练习记录
    List<PracticeSession> findByStudentIdAndPaperIdAndSubmitted(Long studentId, Long paperId, Boolean submitted);
    
    // 查询特定学生特定试卷的未提交练习记录
    Optional<PracticeSession> findFirstByStudentIdAndPaperIdAndSubmittedFalse(Long studentId, Long paperId);
} 