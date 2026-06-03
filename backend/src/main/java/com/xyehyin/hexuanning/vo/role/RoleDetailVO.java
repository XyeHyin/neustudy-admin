package com.xyehyin.hexuanning.vo.role;

import com.xyehyin.hexuanning.vo.permission.PermissionVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RoleDetailVO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Set<PermissionVO> permissions;
}
