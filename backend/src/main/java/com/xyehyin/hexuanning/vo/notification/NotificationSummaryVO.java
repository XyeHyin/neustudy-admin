package com.xyehyin.hexuanning.vo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryVO {
    private List<NotificationVO> notifications;
    private long unreadCount;
    private Long latestReadEventLogId;
}
