package com.jiangong.nmb.dto.user;

import com.jiangong.nmb.constant.RegexConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6")
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    @NotBlank(message = "邮箱不能为空")
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = RegexConstant.PHONE_REGEX, message = "手机号格式不正确")
    private String phone;
    @Pattern(regexp = RegexConstant.IMAGE_URL_REGEX, message = "头像链接格式不正确")
    private String avatar;
    private Long roleId;
}
