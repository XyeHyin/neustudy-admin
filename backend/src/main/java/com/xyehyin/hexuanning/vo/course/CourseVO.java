package com.xyehyin.hexuanning.vo.course;

import com.xyehyin.hexuanning.entity.Course;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程简要信息VO
 */
@Data
public class CourseVO {
    private Long id;
    private String name;
    private String description;
    private String subject;
    private String grade;
    private String semester;
    private Boolean enabled;
    private String coverImage;
    private Integer totalHours;
    private Integer completedHours;
    private Course.CourseStatus status;
    private String teacherName;
    private Long teacherId;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}