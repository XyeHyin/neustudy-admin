package com.xyehyin.hexuanning.vo.knowledgepoint;

import com.xyehyin.hexuanning.entity.KnowledgePoint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "知识点VO")
public class KnowledgePointVO {

    @Schema(description = "知识点ID")
    private Long id;

    @Schema(description = "知识点名称")
    private String name;

    @Schema(description = "知识点描述")
    private String description;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "难度")
    private KnowledgePoint.Difficulty difficulty;

    @Schema(description = "难度描述")
    private String difficultyDescription;

    @Schema(description = "排序号")
    private Integer orderNum;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "知识点内容")
    private String content;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}