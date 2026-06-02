package com.jiangong.nmb.vo.category;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryTreeVO {

    private Long id;

    private String name;

    private List<CategoryTreeVO> children;

    private String description;

    private Boolean enabled = true;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
