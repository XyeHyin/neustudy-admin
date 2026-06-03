package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.entity.EventLog;
import com.xyehyin.hexuanning.service.EventLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 事件日志管理 API
 */
@RestController
@RequestMapping("/event-logs")
@Tag(name = "事件日志", description = "事件记录查询接口")
@RequiredArgsConstructor
public class EventLogController {

    private final EventLogService eventLogService;

    @Operation(summary = "事件日志列表", description = "分页获取事件记录")
    @GetMapping
    public ApiResponse<PageResult<EventLog>> listEventLogs(
            @Parameter(description = "页码，从1开始", required = false)
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(eventLogService.listEventLogs(page, size));
    }
} 