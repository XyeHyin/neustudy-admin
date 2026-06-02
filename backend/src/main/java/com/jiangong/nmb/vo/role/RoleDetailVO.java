package com.jiangong.nmb.vo.role;

import com.jiangong.nmb.vo.permission.PermissionVO;
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
