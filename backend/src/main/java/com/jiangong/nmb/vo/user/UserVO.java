package com.jiangong.nmb.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
