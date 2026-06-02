package com.jiangong.nmb.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.common.PageResult;
import com.jiangong.nmb.constant.PermissionConstants;
import com.jiangong.nmb.dto.user.CreateUserDTO;
import com.jiangong.nmb.dto.user.UpdateOwnPasswordDTO;
import com.jiangong.nmb.dto.user.UpdateUserDTO;
import com.jiangong.nmb.dto.user.UpdateUserPasswordDTO;
import com.jiangong.nmb.entity.Role;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.mapper.UserMapper;
import com.jiangong.nmb.service.RoleService;
import com.jiangong.nmb.service.UserService;
import com.jiangong.nmb.utils.PasswordUtil;
import com.jiangong.nmb.vo.user.UserDetailVO;
import com.jiangong.nmb.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "用户管理", description = "用户相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController extends BaseController {
    private final UserService userService;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Operation(summary = "获取所有用户", description = "获取所有用户")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_LIST_ALL + "')")
    @GetMapping
    public ApiResponse<List<UserVO>> list() {
        return ApiResponse.success(userService.findAll().stream().map(userMapper::toUserVO).toList());
    }

    @Operation(summary = "分页获取用户列表", description = "分页获取用户列表")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_LIST_ALL + "')")
    @GetMapping("/page")
    public ApiResponse<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "2") Integer enabled
    ) {
        Boolean enabledValue = null;
        if (enabled != null) {
            if (enabled == 0) enabledValue = false;
            else if (enabled == 1) enabledValue = true;
        }
        Page<User> userPage = userService.findPage(page, size, keyword, enabledValue);
        List<UserVO> userVOList = userPage.getContent().stream().map(userMapper::toUserVO).toList();
        PageResult<UserVO> result = new PageResult<>();
        result.setContent(userVOList);
        result.setTotal(userPage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取所有教师", description = "获取所有教师用户列表")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_LIST_ALL + "')")
    @GetMapping("/teachers")
    public ApiResponse<List<UserVO>> listTeachers() {
        List<User> teachers = userService.findByRoleName("teacher");
        List<UserVO> teacherVOs = teachers.stream()
                .map(userMapper::toUserVO)
                .toList();
        return ApiResponse.success(teacherVOs);
    }

    @Operation(summary = "分页获取教师列表", description = "分页获取教师用户列表")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_LIST_ALL + "')")
    @GetMapping("/teachers/page")
    public ApiResponse<PageResult<UserVO>> pageTeachers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "2") Integer enabled
    ) {
        Page<User> userPage = userService.findTeacherPageWithFilters(page, size, keyword, enabled);
        List<UserVO> userVOs = userPage.getContent().stream()
                .map(userMapper::toUserVO)
                .toList();

        PageResult<UserVO> result = new PageResult<>();
        result.setContent(userVOs);
        result.setTotal(userPage.getTotalElements());
        result.setPage(page);
        result.setSize(size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "创建用户", description = "创建用户")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_CREATE_ALL + "')")
    @PostMapping
    public ApiResponse<UserVO> create(@RequestBody @Valid CreateUserDTO createUserDTO) {
        Role role = roleService.findById(createUserDTO.getRoleId());
        if (role == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "角色不存在");
        }
        if (userService.findByUsername(createUserDTO.getUsername()) != null) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "用户名已存在");
        }
        User user = userMapper.toUser(createUserDTO);
        user.setPassword(PasswordUtil.encode(createUserDTO.getPassword()));
        user.setRole(role);
        user = userService.save(user);
        return ApiResponse.success(userMapper.toUserVO(user));
    }

    @Operation(summary = "删除用户", description = "删除用户")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_DELETE_ALL + "')")
    @DeleteMapping("/{userId}")
    public ApiResponse<Boolean> delete(@PathVariable Long userId) {
        if (!userService.delete(userId)) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        return ApiResponse.success(true);
    }

    @Operation(summary = "批量删除用户", description = "批量删除用户")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_DELETE_ALL + "')")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> batchDelete(@RequestBody List<Long> userIds) {
        boolean result = userService.batchDelete(userIds);
        return ApiResponse.success(result);
    }

    @Operation(summary = "修改用户信息", description = "修改用户信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_EDIT_ALL + "')")
    @PutMapping("/{userId}")
    public ApiResponse<UserVO> updateProfile(@PathVariable Long userId, @RequestBody @Valid UpdateUserDTO updateUserDTO) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        userMapper.updateUserFromDto(updateUserDTO, user);
        user = userService.save(user);
        return ApiResponse.success(userMapper.toUserVO(user));
    }

    @Operation(summary = "修改个人信息", description = "修改个人信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_EDIT_SELF + "')")
    @PutMapping
    public ApiResponse<UserVO> updateOwnProfile(HttpServletRequest request, @RequestBody @Valid UpdateUserDTO updateUserDTO) {
        User user = userService.findById(getCurrentUserId(request));
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        userMapper.updateUserFromDto(updateUserDTO, user);
        user = userService.save(user);
        return ApiResponse.success(userMapper.toUserVO(user));
    }

    @Operation(summary = "修改个人密码", description = "修改个人密码")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_UPDATE_PASSWORD_SELF + "')")
    @PutMapping("/password")
    public ApiResponse<Boolean> updateOwnPassword(@RequestBody @Valid UpdateUserPasswordDTO updateUserPasswordDTO, HttpServletRequest request) {
        User user = userService.findById(getCurrentUserId(request));
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        if (!PasswordUtil.matches(updateUserPasswordDTO.getOldPassword(), user.getPassword())) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "旧密码错误");
        }
        if (PasswordUtil.matches(updateUserPasswordDTO.getNewPassword(), user.getPassword())) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "新密码不能与旧密码相同");
        }
        user.setPassword(PasswordUtil.encode(updateUserPasswordDTO.getNewPassword()));
        userService.save(user);
        return ApiResponse.success(true);
    }

    @Operation(summary = "修改用户密码", description = "修改用户密码")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_UPDATE_PASSWORD_ALL + "')")
    @PutMapping("/{userId}/password")
    public ApiResponse<Boolean> updatePassword(@PathVariable Long userId, @RequestBody @Valid UpdateOwnPasswordDTO updateUserPasswordDTO) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        if (PasswordUtil.matches(updateUserPasswordDTO.getNewPassword(), user.getPassword())) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "新密码不能与旧密码相同");
        }
        user.setPassword(PasswordUtil.encode(updateUserPasswordDTO.getNewPassword()));
        userService.save(user);
        return ApiResponse.success(true);
    }

    @Operation(summary = "修改用户状态", description = "启用或禁用用户")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_UPDATE_STATUS + "')")
    @PutMapping("/{userId}/status")
    public ApiResponse<Boolean> updateStatus(@PathVariable Long userId, @RequestParam Boolean enabled) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        user.setEnabled(enabled);
        userService.save(user);
        return ApiResponse.success(true);
    }

    @Operation(summary = "修改用户角色", description = "修改用户角色")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_UPDATE_ROLE + "')")
    @PutMapping("/{userId}/role/{roleId}")
    public ApiResponse<UserDetailVO> updateRole(@PathVariable Long userId, @PathVariable Long roleId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        Role role = roleService.findById(roleId);
        if (role == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "角色不存在");
        }
        user.setRole(role);
        userService.save(user);
        return ApiResponse.success(userMapper.toUserDetailVO(user));
    }

    @Operation(summary = "获取用户详情", description = "获取用户详情")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_VIEW_ALL + "')")
    @GetMapping("/{userId}")
    public ApiResponse<UserDetailVO> detail(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        return ApiResponse.success(userMapper.toUserDetailVO(user));
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息")
    @PreAuthorize("hasAuthority('" + PermissionConstants.USER_VIEW_SELF + "')")
    @GetMapping("/me")
    public ApiResponse<UserDetailVO> me(HttpServletRequest request) {
        User user = userService.findById(getCurrentUserId(request));
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        return ApiResponse.success(userMapper.toUserDetailVO(user));
    }
}
