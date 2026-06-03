package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService extends BaseService<Course, Long> {

    private final CourseRepository courseRepository;
    private final KnowledgePointService knowledgePointService;

    @Override
    protected CourseRepository getRepository() {
        return courseRepository;
    }

    /**
     * 根据教师ID查找课程
     */
    public List<Course> findByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    /**
     * 根据学科查找课程
     */
    public List<Course> findBySubject(String subject) {
        return courseRepository.findBySubject(subject);
    }

    /**
     * 根据年级查找课程
     */
    public List<Course> findByGrade(String grade) {
        return courseRepository.findByGrade(grade);
    }

    /**
     * 根据课程状态查找课程
     */
    public List<Course> findByStatus(Course.CourseStatus status) {
        return courseRepository.findByStatus(status);
    }

    /**
     * 统计教师的课程数量
     */
    public long countByTeacherId(Long teacherId) {
        return courseRepository.countByTeacherId(teacherId);
    }

    /**
     * 检查课程名称是否存在（同一教师下）
     */
    public boolean existsByNameAndTeacherId(String name, Long teacherId) {
        return courseRepository.existsByNameAndTeacherId(name, teacherId);
    }

    /**
     * 分页查询所有课程（管理员用）
     */
    public Page<Course> findPageWithFilters(int page, int size, String keyword, Boolean enabled, String subject, String grade, Course.CourseStatus status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return courseRepository.findAllWithFilters(keyword, enabled, subject, grade, status, pageable);
    }

    /**
     * 分页查询教师的课程
     */
    public Page<Course> findPageByTeacherWithFilters(Long teacherId, int page, int size, String keyword, Boolean enabled, String subject, String grade, Course.CourseStatus status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return courseRepository.findByTeacherIdWithFilters(teacherId, keyword, enabled, subject, grade, status, pageable);
    }

    /**
     * 获取所有学科列表
     */
    public List<String> getAllSubjects() {
        return courseRepository.findDistinctSubjects();
    }

    /**
     * 获取所有年级列表
     */
    public List<String> getAllGrades() {
        return courseRepository.findDistinctGrades();
    }

    /**
     * 更新课程进度
     */
    public Course updateProgress(Long courseId, Integer completedHours) {
        Course course = findById(courseId);
        if (course != null) {
            course.setCompletedHours(completedHours);

            // 自动更新课程状态
            if (course.getTotalHours() != null && completedHours != null) {
                if (completedHours >= course.getTotalHours()) {
                    course.setStatus(Course.CourseStatus.COMPLETED);
                } else if (course.getStatus() == Course.CourseStatus.COMPLETED) {
                    course.setStatus(Course.CourseStatus.PUBLISHED);
                }
            }

            return save(course);
        }
        return null;
    }

    /**
     * 发布课程
     */
    public Course publishCourse(Long courseId) {
        Course course = findById(courseId);
        if (course != null && course.getStatus() == Course.CourseStatus.DRAFT) {
            course.setStatus(Course.CourseStatus.PUBLISHED);
            return save(course);
        }
        return course;
    }

    /**
     * 归档课程
     */
    public Course archiveCourse(Long courseId) {
        Course course = findById(courseId);
        if (course != null) {
            course.setStatus(Course.CourseStatus.ARCHIVED);
            course.setEnabled(false);
            return save(course);
        }
        return course;
    }

    /**
     * 删除课程（级联删除知识点）
     */
    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            // 先删除该课程下的所有知识点
            knowledgePointService.deleteByCourseId(id);
            // 再删除课程
            return super.delete(id);
        } catch (Exception e) {
            log.error("删除课程失败：{}", e.getMessage(), e);
            throw new RuntimeException("删除课程失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量删除课程（级联删除知识点）
     */
    @Override
    @Transactional
    public boolean batchDelete(List<Long> courseIds) {
        try {
            // 先删除这些课程下的所有知识点
            for (Long courseId : courseIds) {
                knowledgePointService.deleteByCourseId(courseId);
            }
            // 再删除课程
            courseRepository.deleteAllById(courseIds);
            return true;
        } catch (Exception e) {
            log.error("批量删除课程失败：{}", e.getMessage(), e);
            return false;
        }
    }
}