package com.xyehyin.hexuanning.dto.knowledgepoint;

import com.xyehyin.hexuanning.entity.KnowledgePoint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
@Schema(description = "创建知识点DTO")
public class CreateKnowledgePointDTO {

    @NotBlank(message = "知识点名称不能为空")
    @Size(max = 100, message = "知识点名称长度不能超过100个字符")
    @Schema(description = "知识点名称", example = "Java基础语法")
    private String name;

    @Size(max = 500, message = "知识点描述长度不能超过500个字符")
    @Schema(description = "知识点描述", example = "Java编程语言的基础语法知识")
    private String description;

    @NotNull(message = "所属课程不能为空")
    @Schema(description = "所属课程ID", example = "1")
    private Long courseId;

    @Schema(description = "难易程度", example = "MEDIUM")
    private KnowledgePoint.Difficulty difficulty = KnowledgePoint.Difficulty.MEDIUM;

    @Schema(description = "排序号", example = "1")
    private Integer orderNum;

    @Size(max = 1000, message = "知识点内容长度不能超过1000个字符")
    @Schema(description = "知识点内容", example = "详细的知识点内容")
    private String content;

    @Size(max = 200, message = "关键词长度不能超过200个字符")
    @Schema(description = "关键词", example = "Java,语法,基础")
    private String keywords;
}