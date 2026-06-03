package com.xyehyin.hexuanning.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.constant.PermissionConstants;
import com.xyehyin.hexuanning.dto.course.CreateCourseDTO;
import com.xyehyin.hexuanning.dto.course.UpdateCourseDTO;
import com.xyehyin.hexuanning.entity.Category;
import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.mapper.CourseMapper;
import com.xyehyin.hexuanning.service.CategoryService;
import com.xyehyin.hexuanning.service.CourseService;
import com.xyehyin.hexuanning.service.UserService;
import com.xyehyin.hexuanning.vo.course.CourseDetailVO;
import com.xyehyin.hexuanning.vo.course.CourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "课程管理", description = "课程相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/courses")
@CrossOrigin
public class CourseController extends BaseController {

    private final CourseService courseService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseMapper courseMapper;

    @Operation(summary = "获取所有课程", description = "管理员查看所有课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<CourseVO>> listAll() {
        List<Course> courses = courseService.findAll();
        List<CourseVO> courseVOs = courses.stream()
                .map(courseMapper::toCourseVO)
                .toList();
        return ApiResponse.success(courseVOs);
    }

    @Operation(summary = "获取我的课程", description = "教师查看自己的课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_LIST_SELF + "')")
    @GetMapping("/my")
    public ApiResponse<List<CourseVO>> listMy(HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);
        List<Course> courses = courseService.findByTeacherId(teacherId);
        List<CourseVO> courseVOs = courses.stream()
                .map(courseMapper::toCourseVO)
                .toList();
        return ApiResponse.success(courseVOs);
    }

    @Operation(summary = "分页获取所有课程", description = "管理员分页查看所有课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_LIST_ALL + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<CourseVO>> pageAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Course.CourseStatus status) {

        Page<Course> coursePage = courseService.findPageWithFilters(page, size, keyword, enabled, subject, grade, status);
        List<CourseVO> courseVOs = coursePage.getContent().stream()
                .map(courseMapper::toCourseVO)
                .toList();

        PageResult<CourseVO> result = new PageResult<>();
        result.setContent(courseVOs);
        result.setTotal(coursePage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "分页获取我的课程", description = "教师分页查看自己的课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_LIST_SELF + "')")
    @GetMapping("/my/page")
    public ApiResponse<PageResult<CourseVO>> pageMy(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Course.CourseStatus status) {

        Long teacherId = getCurrentUserId(request);
        Page<Course> coursePage = courseService.findPageByTeacherWithFilters(teacherId, page, size, keyword, enabled, subject, grade, status);
        List<CourseVO> courseVOs = coursePage.getContent().stream()
                .map(courseMapper::toCourseVO)
                .toList();

        PageResult<CourseVO> result = new PageResult<>();
        result.setContent(courseVOs);
        result.setTotal(coursePage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "创建课程", description = "教师创建自己的课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_CREATE_SELF + "')")
    @PostMapping
    public ApiResponse<CourseVO> create(@RequestBody @Valid CreateCourseDTO createCourseDTO, HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);
        User teacher = userService.findById(teacherId);
        if (teacher == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "教师不存在");
        }

        // 检查同一教师下课程名称是否重复
        if (courseService.existsByNameAndTeacherId(createCourseDTO.getName(), teacherId)) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "课程名称已存在");
        }

        Course course = courseMapper.toCourse(createCourseDTO);
        course.setTeacher(teacher);

        // 设置分类（如果提供）
        if (createCourseDTO.getCategoryId() != null) {
            Category category = categoryService.findById(createCourseDTO.getCategoryId());
            if (category == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "分类不存在");
            }
            course.setCategory(category);
        }

        course = courseService.save(course);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "管理员创建课程", description = "管理员为任意教师创建课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_CREATE_ALL + "')")
    @PostMapping("/admin")
    public ApiResponse<CourseVO> createByAdmin(@RequestBody @Valid CreateCourseDTO createCourseDTO, @RequestParam Long teacherId) {
        User teacher = userService.findById(teacherId);
        if (teacher == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "教师不存在");
        }

        // 检查同一教师下课程名称是否重复
        if (courseService.existsByNameAndTeacherId(createCourseDTO.getName(), teacherId)) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "该教师下已存在同名课程");
        }

        Course course = courseMapper.toCourse(createCourseDTO);
        course.setTeacher(teacher);

        // 设置分类（如果提供）
        if (createCourseDTO.getCategoryId() != null) {
            Category category = categoryService.findById(createCourseDTO.getCategoryId());
            if (category == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "分类不存在");
            }
            course.setCategory(category);
        }

        course = courseService.save(course);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "获取课程详情", description = "获取课程详细信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_VIEW_ALL + "') or " +
            "hasAuthority('" + PermissionConstants.COURSE_VIEW_SELF + "')")
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailVO> detail(@PathVariable Long courseId, HttpServletRequest request) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 检查权限：如果只有查看自己的权限，需要验证所有权
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        
        boolean hasViewAll = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(PermissionConstants.COURSE_VIEW_ALL));
        
        if (!hasViewAll) {
            Long currentUserId = getCurrentUserId(request);
            if (!course.getTeacher().getId().equals(currentUserId)) {
                throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能查看自己的课程");
            }
        }
        
        return ApiResponse.success(courseMapper.toCourseDetailVO(course));
    }

    @Operation(summary = "更新课程", description = "教师更新自己的课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_EDIT_SELF + "')")
    @PutMapping("/{courseId}")
    public ApiResponse<CourseVO> update(@PathVariable Long courseId, 
                                       @RequestBody @Valid UpdateCourseDTO updateCourseDTO,
                                       HttpServletRequest request) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 检查权限：验证是否是自己的课程
        Long currentUserId = getCurrentUserId(request);
        if (!course.getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能编辑自己的课程");
        }

        // 检查课程名称是否重复（排除自身）
        if (!course.getName().equals(updateCourseDTO.getName()) &&
                courseService.existsByNameAndTeacherId(updateCourseDTO.getName(), course.getTeacher().getId())) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "课程名称已存在");
        }

        courseMapper.updateCourseFromDto(updateCourseDTO, course);

        // 更新分类
        if (updateCourseDTO.getCategoryId() != null) {
            Category category = categoryService.findById(updateCourseDTO.getCategoryId());
            if (category == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "分类不存在");
            }
            course.setCategory(category);
        } else {
            course.setCategory(null);
        }

        course = courseService.save(course);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "管理员更新课程", description = "管理员更新任意课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_EDIT_ALL + "')")
    @PutMapping("/admin/{courseId}")
    public ApiResponse<CourseVO> updateByAdmin(@PathVariable Long courseId, @RequestBody @Valid UpdateCourseDTO updateCourseDTO) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }

        courseMapper.updateCourseFromDto(updateCourseDTO, course);

        // 更新分类
        if (updateCourseDTO.getCategoryId() != null) {
            Category category = categoryService.findById(updateCourseDTO.getCategoryId());
            if (category == null) {
                throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "分类不存在");
            }
            course.setCategory(category);
        } else {
            course.setCategory(null);
        }

        course = courseService.save(course);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "删除课程", description = "教师删除自己的课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_DELETE_SELF + "')")
    @DeleteMapping("/{courseId}")
    public ApiResponse<Boolean> delete(@PathVariable Long courseId, HttpServletRequest request) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 检查权限：验证是否是自己的课程
        Long currentUserId = getCurrentUserId(request);
        if (!course.getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能删除自己的课程");
        }
        
        return ApiResponse.success(courseService.delete(courseId));
    }

    @Operation(summary = "管理员删除课程", description = "管理员删除任意课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_DELETE_ALL + "')")
    @DeleteMapping("/admin/{courseId}")
    public ApiResponse<Boolean> deleteByAdmin(@PathVariable Long courseId) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        return ApiResponse.success(courseService.delete(courseId));
    }

    @Operation(summary = "批量删除课程", description = "批量删除课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_DELETE_ALL + "')")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDelete(@RequestBody List<Long> courseIds) {
        boolean result = courseService.batchDelete(courseIds);
        return ApiResponse.success(result);
    }

    @Operation(summary = "更新课程进度", description = "更新课程完成进度")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_EDIT_SELF + "')")
    @PutMapping("/{courseId}/progress")
    public ApiResponse<CourseVO> updateProgress(@PathVariable Long courseId, 
                                               @RequestParam Integer completedHours,
                                               HttpServletRequest request) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 检查权限：验证是否是自己的课程
        Long currentUserId = getCurrentUserId(request);
        if (!course.getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能更新自己的课程进度");
        }
        
        course = courseService.updateProgress(courseId, completedHours);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "发布课程", description = "发布课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_EDIT_SELF + "')")
    @PutMapping("/{courseId}/publish")
    public ApiResponse<CourseVO> publishCourse(@PathVariable Long courseId, HttpServletRequest request) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 检查权限：验证是否是自己的课程
        Long currentUserId = getCurrentUserId(request);
        if (!course.getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能发布自己的课程");
        }
        
        course = courseService.publishCourse(courseId);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "归档课程", description = "归档课程")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_EDIT_SELF + "')")
    @PutMapping("/{courseId}/archive")
    public ApiResponse<CourseVO> archiveCourse(@PathVariable Long courseId, HttpServletRequest request) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "课程不存在");
        }
        
        // 检查权限：验证是否是自己的课程
        Long currentUserId = getCurrentUserId(request);
        if (!course.getTeacher().getId().equals(currentUserId)) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "您只能归档自己的课程");
        }
        
        course = courseService.archiveCourse(courseId);
        return ApiResponse.success(courseMapper.toCourseVO(course));
    }

    @Operation(summary = "获取学科列表", description = "获取所有学科")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_LIST_ALL + "') or hasAuthority('" + PermissionConstants.COURSE_LIST_SELF + "')")
    @GetMapping("/subjects")
    public ApiResponse<List<String>> getSubjects() {
        return ApiResponse.success(courseService.getAllSubjects());
    }

    @Operation(summary = "获取年级列表", description = "获取所有年级")
    @PreAuthorize("hasAuthority('" + PermissionConstants.COURSE_LIST_ALL + "') or hasAuthority('" + PermissionConstants.COURSE_LIST_SELF + "')")
    @GetMapping("/grades")
    public ApiResponse<List<String>> getGrades() {
        return ApiResponse.success(courseService.getAllGrades());
    }

    @Operation(summary = "获取课程统计", description = "获取课程统计信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.STATISTICS_COURSE + "')")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(HttpServletRequest request) {
        Long teacherId = getCurrentUserId(request);

        long totalCourses = courseService.countByTeacherId(teacherId);
        long draftCourses = courseService.findByStatus(Course.CourseStatus.DRAFT).size();
        long publishedCourses = courseService.findByStatus(Course.CourseStatus.PUBLISHED).size();
        long completedCourses = courseService.findByStatus(Course.CourseStatus.COMPLETED).size();

        Map<String, Object> statistics = Map.of(
                "totalCourses", totalCourses,
                "draftCourses", draftCourses,
                "publishedCourses", publishedCourses,
                "completedCourses", completedCourses
        );

        return ApiResponse.success(statistics);
    }
}