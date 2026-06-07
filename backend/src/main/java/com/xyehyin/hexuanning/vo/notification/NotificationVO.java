package com.xyehyin.hexuanning.vo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {
    private Long id;
    private Long eventLogId;
    private Long userId;
    private String username;
    private String httpMethod;
    private String uri;
    private String description;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private boolean read;
}
