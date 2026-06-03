package com.xyehyin.hexuanning.vo.course;

import com.xyehyin.hexuanning.entity.Course;
import com.xyehyin.hexuanning.vo.user.UserSimpleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程简单VO
 */
@Data
@Schema(description = "课程简单VO")
public class CourseSimpleVO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程名称")
    private String name;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "学期")
    private String semester;

    @Schema(description = "课程状态")
    private Course.CourseStatus status;

    @Schema(description = "教师")
    private UserSimpleVO teacher;
}