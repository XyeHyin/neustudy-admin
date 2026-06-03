package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.common.PageResult;
import com.xyehyin.hexuanning.entity.EventLog;
import com.xyehyin.hexuanning.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
/**
 * 事件日志服务，用于记录并存储用户操作事件
 */
@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    /**
     * 记录一个用户事件
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param httpMethod  HTTP 方法
     * @param uri         请求路径
     * @param description 事件描述
     */
    public void logEvent(Long userId, String username, String httpMethod, String uri, String description) {
        EventLog event = EventLog.builder()
                .userId(userId)
                .username(username)
                .httpMethod(httpMethod)
                .uri(uri)
                .description(description)
                .createTime(LocalDateTime.now())
                .build();
        eventLogRepository.save(event);
    }

    /**
     * 分页查询事件日志
     *
     * @param page 页码，从1开始
     * @param size 每页条数
     * @return 分页事件日志列表
     */
    public PageResult<EventLog> listEventLogs(int page, int size) {
        int p = Math.max(page, 1) - 1;
        int s = Math.max(size, 1);
        Page<EventLog> pageResult = eventLogRepository.findAll(
                PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createTime")));
        return PageResult.<EventLog>builder()
                .content(pageResult.getContent())
                .total(pageResult.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
} 