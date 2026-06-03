package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识点数据访问层
 */
@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {

    /**
     * 根据课程查找知识点
     */
    List<KnowledgePoint> findByCourse(Course course);

    /**
     * 根据课程ID查找知识点
     */
    List<KnowledgePoint> findByCourseId(Long courseId);

    /**
     * 根据课程ID和启用状态查找知识点
     */
    List<KnowledgePoint> findByCourseIdAndEnabled(Long courseId, Boolean enabled);

    /**
     * 根据知识点名称查找（模糊匹配）
     */
    List<KnowledgePoint> findByNameContainingIgnoreCase(String name);

    /**
     * 根据难度查找知识点
     */
    List<KnowledgePoint> findByDifficulty(KnowledgePoint.Difficulty difficulty);

    /**
     * 根据启用状态查找知识点
     */
    List<KnowledgePoint> findByEnabled(Boolean enabled);

    /**
     * 统计课程的知识点数量
     */
    long countByCourseId(Long courseId);

    /**
     * 检查知识点名称是否存在（同一课程下）
     */
    boolean existsByNameAndCourseId(String name, Long courseId);

    /**
     * 分页查询所有知识点（支持关键字搜索）
     */
    @Query("SELECT kp FROM KnowledgePoint kp WHERE " +
           "(:keyword IS NULL OR kp.name LIKE %:keyword% OR kp.description LIKE %:keyword% OR kp.content LIKE %:keyword%) " +
           "AND (:enabled IS NULL OR kp.enabled = :enabled) " +
           "AND (:courseId IS NULL OR kp.course.id = :courseId) " +
           "AND (:difficulty IS NULL OR kp.difficulty = :difficulty)")
    Page<KnowledgePoint> findAllWithFilters(
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            @Param("courseId") Long courseId,
            @Param("difficulty") KnowledgePoint.Difficulty difficulty,
            Pageable pageable
    );

    /**
     * 分页查询教师的知识点（通过课程关联）
     */
    @Query("SELECT kp FROM KnowledgePoint kp WHERE kp.course.teacher.id = :teacherId " +
           "AND (:keyword IS NULL OR kp.name LIKE %:keyword% OR kp.description LIKE %:keyword% OR kp.content LIKE %:keyword%) " +
           "AND (:enabled IS NULL OR kp.enabled = :enabled) " +
           "AND (:courseId IS NULL OR kp.course.id = :courseId) " +
           "AND (:difficulty IS NULL OR kp.difficulty = :difficulty)")
    Page<KnowledgePoint> findByTeacherIdWithFilters(
            @Param("teacherId") Long teacherId,
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            @Param("courseId") Long courseId,
            @Param("difficulty") KnowledgePoint.Difficulty difficulty,
            Pageable pageable
    );

    /**
     * 获取课程下知识点的最大排序号
     */
    @Query("SELECT MAX(kp.orderNum) FROM KnowledgePoint kp WHERE kp.course.id = :courseId")
    Integer findMaxOrderNumByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据课程ID排序查找知识点
     */
    List<KnowledgePoint> findByCourseIdOrderByOrderNumAsc(Long courseId);

    /**
     * 根据课程ID删除所有知识点
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM KnowledgePoint kp WHERE kp.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}