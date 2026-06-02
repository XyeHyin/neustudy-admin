package com.jiangong.nmb.dto.course;

import com.jiangong.nmb.entity.Course;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新课程DTO
 */
@Data
public class UpdateCourseDTO {
    
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    private String name;
    
    @Size(max = 500, message = "课程描述长度不能超过500个字符")
    private String description;
    
    @Size(max = 50, message = "学科名称长度不能超过50个字符")
    private String subject;
    
    @Size(max = 20, message = "年级长度不能超过20个字符")
    private String grade;
    
    @Size(max = 20, message = "学期长度不能超过20个字符")
    private String semester;
    
    private Boolean enabled;
    
    private Long categoryId;
    
    @Size(max = 255, message = "封面图片URL长度不能超过255个字符")
    private String coverImage;
    
    private Integer totalHours;
    
    private Integer completedHours;
    
    private Course.CourseStatus status;
}