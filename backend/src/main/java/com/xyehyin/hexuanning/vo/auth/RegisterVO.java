package com.xyehyin.hexuanning.vo.auth;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterVO {
    private Long id;
    private String token;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
