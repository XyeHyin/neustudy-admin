package com.xyehyin.hexuanning.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name; // 分类名称

    private Long parentId; // 父级分类（可选）

    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description; // 描述（可选）
}
