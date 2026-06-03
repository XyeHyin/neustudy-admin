package com.xyehyin.hexuanning.vo.category;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryFlatVO {

    private Long id;

    private String name;

    private String description;

    private CategoryFlatVO parent;

    private Boolean enabled = true;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
