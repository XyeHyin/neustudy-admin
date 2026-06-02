package com.jiangong.nmb.vo.knowledgepoint;

import com.jiangong.nmb.entity.KnowledgePoint;
import com.jiangong.nmb.vo.course.CourseSimpleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识点简单VO
 */
@Data
@Schema(description = "知识点简单VO")
public class KnowledgePointSimpleVO {

    @Schema(description = "知识点ID")
    private Long id;

    @Schema(description = "知识点名称")
    private String name;

    @Schema(description = "知识点描述")
    private String description;

    @Schema(description = "难度")
    private KnowledgePoint.Difficulty difficulty;

    @Schema(description = "难度描述")
    private String difficultyDescription;

    @Schema(description = "排序号")
    private Integer orderNum;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "所属课程")
    private CourseSimpleVO course;
}