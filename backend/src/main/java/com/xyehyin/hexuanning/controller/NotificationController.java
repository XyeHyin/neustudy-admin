package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.service.NotificationService;
import com.xyehyin.hexuanning.vo.notification.NotificationSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@Tag(name = "站内通知", description = "基于事件日志的站内通知接口")
@RequiredArgsConstructor
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    @Operation(summary = "通知摘要", description = "获取最近通知与未读数量")
    @GetMapping
    public ApiResponse<NotificationSummaryVO> getSummary(
            HttpServletRequest request,
            @Parameter(description = "返回条数", required = false)
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(notificationService.getSummary(requireCurrentUserId(request), limit));
    }

    @Operation(summary = "实时通知流", description = "使用 Server-Sent Events 推送新的事件日志通知")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletRequest request) {
        return notificationService.connect(requireCurrentUserId(request));
    }

    @Operation(summary = "单条通知标为已读", description = "将指定事件日志通知标记为已读")
    @PostMapping("/{eventLogId}/read")
    public ApiResponse<NotificationSummaryVO> markRead(
            HttpServletRequest request,
            @PathVariable Long eventLogId,
            @Parameter(description = "返回条数", required = false)
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(notificationService.markRead(requireCurrentUserId(request), eventLogId, limit));
    }

    @Operation(summary = "全部通知标为已读", description = "将当前已有通知全部标记为已读")
    @PostMapping("/read-all")
    public ApiResponse<NotificationSummaryVO> markAllRead(
            HttpServletRequest request,
            @Parameter(description = "返回条数", required = false)
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(notificationService.markAllRead(requireCurrentUserId(request), limit));
    }

    private Long requireCurrentUserId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        return userId;
    }
}
