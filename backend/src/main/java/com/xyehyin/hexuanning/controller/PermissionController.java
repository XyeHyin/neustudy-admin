package com.xyehyin.hexuanning.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.constant.PermissionConstants;
import com.xyehyin.hexuanning.entity.Permission;
import com.xyehyin.hexuanning.mapper.PermissionMapper;
import com.xyehyin.hexuanning.service.PermissionService;
import com.xyehyin.hexuanning.vo.permission.PermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "权限管理", description = "权限相关接口")
@RequestMapping("/permissions")
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class PermissionController extends BaseController {
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @Operation(summary = "获取所有权限", description = "获取所有权限")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PERMISSION_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<PermissionVO>> list() {
        return ApiResponse.success(permissionService.findAll().stream().map(permissionMapper::toPermissionVO).toList());
    }

    @Operation(summary = "获取权限详情", description = "获取权限详情")
    @PreAuthorize("hasAuthority('" + PermissionConstants.PERMISSION_VIEW_ALL + "')")
    @GetMapping("/{permissionId}")
    public ApiResponse<PermissionVO> detail(@PathVariable Long permissionId) {
        Permission permission = permissionService.findById(permissionId);
        if (permission == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "权限不存在");
        }
        return ApiResponse.success(permissionMapper.toPermissionVO(permission));
    }
}
