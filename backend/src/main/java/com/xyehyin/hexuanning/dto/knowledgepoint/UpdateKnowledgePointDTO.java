package com.xyehyin.hexuanning.dto.knowledgepoint;

import com.xyehyin.hexuanning.entity.KnowledgePoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateKnowledgePointDTO {

    @NotBlank(message = "知识点名称不能为空")
    @Size(max = 100, message = "知识点名称长度不能超过100个字符")
    private String name; // 知识点名称

    @Size(max = 500, message = "知识点描述长度不能超过500个字符")
    private String description; // 知识点描述

    private KnowledgePoint.Difficulty difficulty; // 难易程度

    private Integer orderNum; // 排序号

    @Size(max = 1000, message = "知识点内容长度不能超过1000个字符")
    private String content; // 知识点内容

    @Size(max = 200, message = "关键词长度不能超过200个字符")
    private String keywords; // 关键词
}