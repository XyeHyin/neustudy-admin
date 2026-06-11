package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程数据访问层
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Override
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findAll();

    @Override
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    Optional<Course> findById(Long id);

    /**
     * 根据教师查找课程
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findByTeacher(User teacher);

    /**
     * 根据教师ID查找课程
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findByTeacherId(Long teacherId);

    /**
     * 根据课程名称查找课程（模糊匹配）
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findByNameContainingIgnoreCase(String name);

    /**
     * 根据学科查找课程
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findBySubject(String subject);

    /**
     * 根据年级查找课程
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findByGrade(String grade);

    /**
     * 根据课程状态查找课程
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findByStatus(Course.CourseStatus status);

    /**
     * 根据启用状态查找课程
     */
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    List<Course> findByEnabled(Boolean enabled);

    /**
     * 统计教师的课程数量
     */
    long countByTeacherId(Long teacherId);

    long countByTeacherIdAndStatus(Long teacherId, Course.CourseStatus status);

    /**
     * 分页查询所有课程（支持关键字搜索）
     */
    @Query("SELECT c FROM Course c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword% OR c.subject LIKE %:keyword%) " +
           "AND (:enabled IS NULL OR c.enabled = :enabled) " +
           "AND (:subject IS NULL OR c.subject = :subject) " +
           "AND (:grade IS NULL OR c.grade = :grade) " +
           "AND (:status IS NULL OR c.status = :status)")
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    Page<Course> findAllWithFilters(
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            @Param("subject") String subject,
            @Param("grade") String grade,
            @Param("status") Course.CourseStatus status,
            Pageable pageable
    );

    /**
     * 分页查询教师的课程（支持关键字搜索）
     */
    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId " +
           "AND (:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword% OR c.subject LIKE %:keyword%) " +
           "AND (:enabled IS NULL OR c.enabled = :enabled) " +
           "AND (:subject IS NULL OR c.subject = :subject) " +
           "AND (:grade IS NULL OR c.grade = :grade) " +
           "AND (:status IS NULL OR c.status = :status)")
    @EntityGraph(attributePaths = {"teacher", "category", "category.parent"})
    Page<Course> findByTeacherIdWithFilters(
            @Param("teacherId") Long teacherId,
            @Param("keyword") String keyword,
            @Param("enabled") Boolean enabled,
            @Param("subject") String subject,
            @Param("grade") String grade,
            @Param("status") Course.CourseStatus status,
            Pageable pageable
    );

    /**
     * 检查课程名称是否存在（同一教师下）
     */
    boolean existsByNameAndTeacherId(String name, Long teacherId);

    /**
     * 获取所有学科列表
     */
    @Query("SELECT DISTINCT c.subject FROM Course c WHERE c.subject IS NOT NULL ORDER BY c.subject")
    List<String> findDistinctSubjects();

    /**
     * 获取所有年级列表
     */
    @Query("SELECT DISTINCT c.grade FROM Course c WHERE c.grade IS NOT NULL ORDER BY c.grade")
    List<String> findDistinctGrades();
}
