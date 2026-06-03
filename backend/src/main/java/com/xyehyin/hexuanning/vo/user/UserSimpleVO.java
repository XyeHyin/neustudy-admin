package com.xyehyin.hexuanning.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户简单VO
 */
@Data
@Schema(description = "用户简单VO")
public class UserSimpleVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;
}