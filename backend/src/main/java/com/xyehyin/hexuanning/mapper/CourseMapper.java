package com.xyehyin.hexuanning.mapper;

import com.xyehyin.hexuanning.dto.course.CreateCourseDTO;
import com.xyehyin.hexuanning.dto.course.UpdateCourseDTO;
import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.vo.course.CourseDetailVO;
import com.xyehyin.hexuanning.vo.course.CourseSimpleVO;
import com.xyehyin.hexuanning.vo.course.CourseVO;
import org.mapstruct.*;

/**
 * 课程映射器
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface CourseMapper {

    /**
     * CreateCourseDTO -> Course
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Course toCourse(CreateCourseDTO createCourseDTO);

    /**
     * Course -> CourseVO
     */
    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "teacher.username", target = "teacherName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    CourseVO toCourseVO(Course course);

    /**
     * Course -> CourseDetailVO
     */
    CourseDetailVO toCourseDetailVO(Course course);

    /**
     * Course -> CourseSimpleVO
     */
    CourseSimpleVO toCourseSimpleVO(Course course);

    /**
     * 更新 Course 从 UpdateCourseDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateCourseFromDto(UpdateCourseDTO updateCourseDTO, @MappingTarget Course course);
}