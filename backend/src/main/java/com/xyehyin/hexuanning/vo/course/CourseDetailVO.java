package com.xyehyin.hexuanning.vo.course;

import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.vo.category.CategoryFlatVO;
import com.xyehyin.hexuanning.vo.user.UserVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程详细信息VO
 */
@Data
public class CourseDetailVO {
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
    private UserVO teacher;
    private CategoryFlatVO category;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 计算进度百分比
    public Double getProgress() {
        if (totalHours == null || totalHours == 0) {
            return 0.0;
        }
        if (completedHours == null) {
            return 0.0;
        }
        return Math.min(100.0, (completedHours * 100.0) / totalHours);
    }
}