package com.xyehyin.hexuanning.controller;

import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.dto.auth.LoginDTO;
import com.xyehyin.hexuanning.dto.auth.ResetPasswordDTO;
import com.xyehyin.hexuanning.dto.auth.SendCodeDTO;
import com.xyehyin.hexuanning.entity.Role;
import com.xyehyin.hexuanning.service.EmailService;
import com.xyehyin.hexuanning.service.RoleService;
import com.xyehyin.hexuanning.vo.auth.LoginVO;
import com.xyehyin.hexuanning.dto.auth.RegisterDTO;
import com.xyehyin.hexuanning.vo.auth.RegisterVO;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.service.UserService;
import com.xyehyin.hexuanning.security.JWTUtil;
import com.xyehyin.hexuanning.security.PasswordUtil;
import com.xyehyin.hexuanning.mapper.AuthMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "认证", description = "登录注册相关接口")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthController extends BaseController {
    private final EmailService emailService;
    private final UserService userService;
    private final AuthMapper authMapper;
    private final RoleService roleService;

    @Operation(summary = "登录", description = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        User user = userService.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_UNAUTHORIZED, "用户不存在或密码错误");
        }
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new StatefulException(HttpStatus.HTTP_UNAUTHORIZED, "用户不存在或密码错误");
        }
        String token = JWTUtil.generateToken(user.getId(), user.getUsername());
        LoginVO loginVO = authMapper.toLoginVO(user, token);
        return ApiResponse.success(loginVO);
    }

    @Operation(summary = "发送验证码", description = "发送验证码")
    @PostMapping("/code")
    public ApiResponse<Void> sendCode(@RequestBody @Valid SendCodeDTO dto) {
        emailService.sendVerifyCode(dto.getEmail());
        return ApiResponse.success();
    }

    @Operation(summary = "注册", description = "用户注册")
    @PostMapping("/register")
    public ApiResponse<RegisterVO> register(@RequestBody @Valid RegisterDTO dto) {
        if (!emailService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "验证码错误或已过期");
        }
        if (userService.findByUsername(dto.getUsername()) != null) {
            throw new StatefulException(HttpStatus.HTTP_CONFLICT, "用户名已存在");
        }
        Role userRole = roleService.findRoleByName("student");
        User user = User.builder().username(dto.getUsername()).password(PasswordUtil.encode(dto.getPassword())).nickname(dto.getNickname()).email(dto.getEmail()).phone(dto.getPhone()).enabled(true).role(userRole).build();
        user = userService.save(user);
        user = userService.findById(user.getId());
        String token = JWTUtil.generateToken(user.getId(), user.getUsername());
        return ApiResponse.success(authMapper.toRegisterVO(user, token));
    }

    @Operation(summary = "重置密码", description = "重置密码")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        if (!emailService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw new StatefulException(HttpStatus.HTTP_BAD_REQUEST, "验证码错误或已过期");
        }
        User user = userService.findByEmail(dto.getEmail());
        if (user == null) {
            throw new StatefulException(HttpStatus.HTTP_NOT_FOUND, "用户不存在");
        }
        user.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        userService.save(user);
        return ApiResponse.success();
    }
}