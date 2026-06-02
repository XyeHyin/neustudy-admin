package com.jiangong.nmb.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.dto.role.CreateRoleDTO;
import com.jiangong.nmb.entity.Role;
import com.jiangong.nmb.mapper.RoleMapper;
import com.jiangong.nmb.service.PermissionService;
import com.jiangong.nmb.service.RoleService;
import com.jiangong.nmb.service.UserService;
import com.jiangong.nmb.vo.role.RoleDetailVO;
import com.jiangong.nmb.vo.permission.PermissionVO;
import com.jiangong.nmb.vo.role.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "角色管理", description = "角色相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@CrossOrigin
public class RoleController {
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final RoleMapper roleMapper;
    private final UserService userService;

    @Operation(summary = "获取所有角色", description = "获取所有角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<RoleVO>> list() {
        return ApiResponse.success(roleService.findAll().stream().map(roleMapper::toRoleVO).toList());
    }

    @Operation(summary = "分页获取角色", description = "分页获取角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_LIST_ALL + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<RoleVO>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Page<Role> userPage = roleService.findPage(page, size);
        List<RoleVO> roleVOList = userPage.getContent().stream().map(roleMapper::toRoleVO).toList();
        PageResult<RoleVO> pageResult = new PageResult<>();
        pageResult.setTotal(userPage.getTotalElements());
        pageResult.setPage(userPage.getNumber());
        pageResult.setSize(userPage.getSize());
        pageResult.setContent(roleVOList);
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "创建角色", description = "创建角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_CREATE_ALL + "')")
    @PostMapping
    public ApiResponse<RoleVO> create(@RequestBody @Valid CreateRoleDTO createRoleDTO) {
        if (roleService.findRoleByName(createRoleDTO.getName()) != null) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "角色已存在");
        }
        Role role = Role.builder()
                .name(createRoleDTO.getName())
                .description(createRoleDTO.getDescription())
                .build();
        role = roleService.create(role, createRoleDTO.getPermissionIds());
        return ApiResponse.success(roleMapper.toRoleVO(role));
    }

    @Operation(summary = "获取角色详情", description = "获取角色详情")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_VIEW_ALL + "')")
    @GetMapping("/{roleId}")
    public ApiResponse<RoleDetailVO> detail(@PathVariable Long roleId) {
        Role role = roleService.findById(roleId);
        if (role == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "角色不存在");
        }
        return ApiResponse.success(roleMapper.toRoleDetailVO(role));
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_DELETE_ALL + "')")
    @DeleteMapping("/{roleId}")
    public ApiResponse<Boolean> delete(@PathVariable Long roleId) {
        long userCount = userService.countByRoleId(roleId);
        if (userCount > 0) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "该角色下还有用户，无法删除");
        }
        if (!roleService.delete(roleId)) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "删除角色失败");
        }
        return ApiResponse.success(true);
    }

    @Operation(summary = "批量删除角色", description = "批量删除角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_DELETE_ALL + "')")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDelete(@RequestBody List<Long> roleIds) {
        for (Long roleId : roleIds) {
            long userCount = userService.countByRoleId(roleId);
            if (userCount > 0) {
                throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "角色ID " + roleId + " 下还有用户，无法删除");
            }
        }
        boolean result = roleService.batchDelete(roleIds);
        return ApiResponse.success(result);
    }

    @Operation(summary = "分配权限", description = "分配权限")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_ASSIGN_ALL + "')")
    @PostMapping("/{roleId}/permissions")
    public ApiResponse<RoleVO> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        Role role = roleService.findById(roleId);
        if (permissionIds.isEmpty()) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "权限列表不能为空");
        }
        if (role == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "角色不存在");
        }
        if (!permissionService.assignPermissions(role, permissionIds)) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "分配权限失败");
        }
        return ApiResponse.success(roleMapper.toRoleVO(role));
    }

    @Operation(summary = "编辑角色", description = "编辑角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.ROLE_EDIT_ALL + "')")
    @PutMapping("/{roleId}")
    public ApiResponse<RoleVO> update(@PathVariable Long roleId, @RequestBody @Valid CreateRoleDTO updateRoleDTO) {
        Role role = roleService.findById(roleId);
        if (role == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "角色不存在");
        }
        if (roleService.findRoleByName(updateRoleDTO.getName()) != null) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "角色已存在");
        }
        role.setName(updateRoleDTO.getName());
        role.setDescription(updateRoleDTO.getDescription());
        role = roleService.update(role, updateRoleDTO.getPermissionIds());
        return ApiResponse.success(roleMapper.toRoleVO(role));
    }
}