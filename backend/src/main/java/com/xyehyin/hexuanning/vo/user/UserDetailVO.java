package com.xyehyin.hexuanning.vo.user;

import com.xyehyin.hexuanning.vo.role.RoleDetailVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDetailVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private RoleDetailVO role;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}