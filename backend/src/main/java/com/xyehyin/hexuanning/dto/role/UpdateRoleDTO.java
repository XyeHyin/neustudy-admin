package com.xyehyin.hexuanning.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleDTO {
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotBlank(message = "角色描述不能为空")
    private String description;

    @NotNull(message = "权限列表不能为空")
    private List<Long> permissionIds;
}