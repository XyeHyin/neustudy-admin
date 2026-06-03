package com.xyehyin.hexuanning.service;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointExportDTO;
import com.xyehyin.hexuanning.dto.knowledgepoint.KnowledgePointImportDTO;
import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.entity.KnowledgePoint;
import com.xyehyin.hexuanning.repository.KnowledgePointRepository;
import com.xyehyin.hexuanning.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KnowledgePointService extends BaseService<KnowledgePoint, Long> {
    
    private final KnowledgePointRepository knowledgePointRepository;
    private final CourseRepository courseRepository;

    public KnowledgePointService(KnowledgePointRepository knowledgePointRepository, CourseRepository courseRepository) {
        this.knowledgePointRepository = knowledgePointRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    protected KnowledgePointRepository getRepository() {
        return knowledgePointRepository;
    }

    /**
     * 根据课程ID查找知识点
     */
    public List<KnowledgePoint> findByCourseId(Long courseId) {
        return knowledgePointRepository.findByCourseId(courseId);
    }

    /**
     * 根据课程ID和启用状态查找知识点
     */
    public List<KnowledgePoint> findByCourseIdAndEnabled(Long courseId, Boolean enabled) {
        return knowledgePointRepository.findByCourseIdAndEnabled(courseId, enabled);
    }

    /**
     * 根据课程ID排序查找知识点
     */
    public List<KnowledgePoint> findByCourseIdOrderByOrderNum(Long courseId) {
        return knowledgePointRepository.findByCourseIdOrderByOrderNumAsc(courseId);
    }

    /**
     * 检查知识点名称是否存在（同一课程下）
     */
    public boolean existsByNameAndCourseId(String name, Long courseId) {
        return knowledgePointRepository.existsByNameAndCourseId(name, courseId);
    }

    /**
     * 统计课程的知识点数量
     */
    public long countByCourseId(Long courseId) {
        return knowledgePointRepository.countByCourseId(courseId);
    }

    /**
     * 分页查询所有知识点（管理员）
     */
    public Page<KnowledgePoint> findPageWithFilters(int page, int size, String keyword, Boolean enabled, 
                                                   Long courseId, KnowledgePoint.Difficulty difficulty) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        return knowledgePointRepository.findAllWithFilters(keyword, enabled, courseId, difficulty, pageable);
    }

    /**
     * 分页查询教师的知识点
     */
    public Page<KnowledgePoint> findPageByTeacherWithFilters(Long teacherId, int page, int size, String keyword, 
                                                           Boolean enabled, Long courseId, KnowledgePoint.Difficulty difficulty) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        return knowledgePointRepository.findByTeacherIdWithFilters(teacherId, keyword, enabled, courseId, difficulty, pageable);
    }

    /**
     * 获取课程下知识点的最大排序号
     */
    public Integer getMaxOrderNumByCourseId(Long courseId) {
        Integer maxOrderNum = knowledgePointRepository.findMaxOrderNumByCourseId(courseId);
        return maxOrderNum != null ? maxOrderNum : 0;
    }

    /**
     * 更新知识点启用状态
     */
    @Transactional
    public KnowledgePoint updateEnabled(Long id, Boolean enabled) {
        KnowledgePoint knowledgePoint = findById(id);
        if (knowledgePoint != null) {
            knowledgePoint.setEnabled(enabled);
            return save(knowledgePoint);
        }
        return null;
    }

    /**
     * 更新知识点排序
     */
    @Transactional
    public KnowledgePoint updateOrder(Long id, Integer orderNum) {
        KnowledgePoint knowledgePoint = findById(id);
        if (knowledgePoint != null) {
            knowledgePoint.setOrderNum(orderNum);
            return save(knowledgePoint);
        }
        return null;
    }

    /**
     * 根据课程ID删除所有知识点（级联删除题目）
     */
    @Transactional
    public boolean deleteByCourseId(Long courseId) {
        try {
            List<KnowledgePoint> knowledgePoints = knowledgePointRepository.findByCourseId(courseId);
            if (!knowledgePoints.isEmpty()) {
                // 删除知识点（会级联删除题目）
                knowledgePointRepository.deleteAll(knowledgePoints);
            }
            return true;
        } catch (Exception e) {
            log.error("删除课程 {} 下的知识点失败", courseId, e);
            return false;
        }
    }

    /**
     * 删除知识点（级联删除题目）
     */
    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            // 由于实体配置了级联删除，直接删除知识点即可
            return super.delete(id);
        } catch (Exception e) {
            log.error("删除知识点失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 导出知识点数据
     */
    public List<KnowledgePointExportDTO> exportKnowledgePoints(Long courseId, Boolean enabled, 
                                                          KnowledgePoint.Difficulty difficulty, Long teacherId) {
        List<KnowledgePoint> knowledgePoints;
        
        if (teacherId != null) {
            // 教师只能导出自己的知识点
            Page<KnowledgePoint> page = knowledgePointRepository.findByTeacherIdWithFilters(
                    teacherId, null, enabled, courseId, difficulty, 
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").ascending()));
            knowledgePoints = page.getContent();
        } else {
            // 管理员可以导出所有知识点
            Page<KnowledgePoint> page = knowledgePointRepository.findAllWithFilters(
                    null, enabled, courseId, difficulty, 
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").ascending()));
            knowledgePoints = page.getContent();
        }
        
        return knowledgePoints.stream()
                .map(this::toExportDTO)
                .toList();
    }

    /**
     * 导入知识点数据
     */
    @Transactional
    public Map<String, Object> importKnowledgePoints(List<KnowledgePointImportDTO> importDTOs,
                                                Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 验证权限
        if (teacherId != null && !course.getTeacher().getId().equals(teacherId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能为自己的课程导入知识点");
        }
        
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>(); // 只包含业务错误，不包含解析警告
        
        for (int i = 0; i < importDTOs.size(); i++) {
            try {
                KnowledgePointImportDTO dto = importDTOs.get(i);
                
                // 验证必填数据
                if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                    errorMessages.add("第" + (i + 2) + "行：知识点名称不能为空");
                    failCount++;
                    continue;
                }
                
                // 检查重复（这是业务错误，不是解析警告）
                if (existsByNameAndCourseId(dto.getName(), courseId)) {
                    errorMessages.add("第" + (i + 2) + "行：知识点名称已存在");
                    failCount++;
                    continue;
                }
                
                // 创建知识点
                KnowledgePoint knowledgePoint = new KnowledgePoint();
                knowledgePoint.setName(dto.getName().trim());
                knowledgePoint.setDescription(dto.getDescription());
                knowledgePoint.setCourse(course);
                knowledgePoint.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : KnowledgePoint.Difficulty.MEDIUM);
                knowledgePoint.setOrderNum(dto.getOrderNum() != null ? dto.getOrderNum() : getMaxOrderNumByCourseId(courseId) + 1);
                knowledgePoint.setContent(dto.getContent());
                knowledgePoint.setKeywords(dto.getKeywords());
                knowledgePoint.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
                
                save(knowledgePoint);
                successCount++;
                
            } catch (Exception e) {
                // 这里捕获的是真正的业务异常，不是解析警告
                errorMessages.add("第" + (i + 2) + "行：" + e.getMessage());
                failCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errorMessages", errorMessages); // 纯业务错误
        
        return result;
    }

    private KnowledgePointExportDTO toExportDTO(KnowledgePoint knowledgePoint) {
        KnowledgePointExportDTO dto = new KnowledgePointExportDTO();
        dto.setName(knowledgePoint.getName());
        dto.setDescription(knowledgePoint.getDescription());
        dto.setCourseName(knowledgePoint.getCourse().getName());
        dto.setDifficulty(knowledgePoint.getDifficulty() != null ? knowledgePoint.getDifficulty().name() : "");
        dto.setOrderNum(knowledgePoint.getOrderNum());
        dto.setContent(knowledgePoint.getContent());
        dto.setKeywords(knowledgePoint.getKeywords());
        dto.setEnabled(knowledgePoint.getEnabled() ? "启用" : "禁用");
        dto.setCreateTime(knowledgePoint.getCreateTime() != null ? 
                knowledgePoint.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        return dto;
    }
}