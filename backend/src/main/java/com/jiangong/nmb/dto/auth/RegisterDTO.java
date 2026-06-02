package com.jiangong.nmb.dto.auth;

import com.jiangong.nmb.constant.RegexConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20之间")
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    @NotBlank(message = "邮箱不能为空")
    @Email
    private String email;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = RegexConstant.PHONE_REGEX, message = "手机号格式不正确")
    private String phone;
    @NotBlank(message = "验证码不能为空")
    private String code;
}
