package com.jiangong.nmb.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "邮箱不能为空")
    @Email
    private String email;
    @NotBlank(message = "验证码不能为空")
    private String code;
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6到20之间")
    private String newPassword;
}
