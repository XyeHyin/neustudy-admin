package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.entity.EventLog;
import com.xyehyin.hexuanning.entity.NotificationRead;
import com.xyehyin.hexuanning.entity.NotificationState;
import com.xyehyin.hexuanning.repository.EventLogRepository;
import com.xyehyin.hexuanning.repository.NotificationReadRepository;
import com.xyehyin.hexuanning.repository.NotificationStateRepository;
import com.xyehyin.hexuanning.vo.notification.NotificationSummaryVO;
import com.xyehyin.hexuanning.vo.notification.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 将事件日志转换为站内通知，并维护在线用户的实时推送连接。
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final long DEFAULT_READ_EVENT_LOG_ID = 0L;
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    private final EventLogRepository eventLogRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final NotificationStateRepository notificationStateRepository;
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<SseEmitter, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "notification-sse-heartbeat");
        thread.setDaemon(true);
        return thread;
    });

    public NotificationSummaryVO getSummary(Long userId, int limit) {
        int size = Math.min(Math.max(limit, 1), 99);
        long latestReadEventLogId = getLatestReadEventLogId(userId);
        var logs = eventLogRepository.findAll(
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();
        Set<Long> individuallyReadEventLogIds = findReadEventLogIds(
                userId,
                logs.stream().map(EventLog::getId).toList()
        );
        var notifications = logs.stream()
                .map(log -> toNotification(log, isRead(log.getId(), latestReadEventLogId, individuallyReadEventLogIds)))
                .toList();

        return NotificationSummaryVO.builder()
                .notifications(notifications)
                .unreadCount(eventLogRepository.countUnreadNotifications(userId, latestReadEventLogId))
                .latestReadEventLogId(latestReadEventLogId)
                .build();
    }

    public NotificationSummaryVO markRead(Long userId, Long eventLogId, int limit) {
        if (eventLogId == null || !eventLogRepository.existsById(eventLogId)) {
            return getSummary(userId, limit);
        }

        long latestReadEventLogId = getLatestReadEventLogId(userId);
        if (eventLogId > latestReadEventLogId
                && !notificationReadRepository.existsByUserIdAndEventLogId(userId, eventLogId)) {
            notificationReadRepository.save(NotificationRead.builder()
                    .userId(userId)
                    .eventLogId(eventLogId)
                    .readTime(LocalDateTime.now())
                    .build());
        }

        return getSummary(userId, limit);
    }

    public NotificationSummaryVO markAllRead(Long userId, int limit) {
        Long latestEventLogId = eventLogRepository.findTopByOrderByIdDesc()
                .map(EventLog::getId)
                .orElse(DEFAULT_READ_EVENT_LOG_ID);
        NotificationState state = notificationStateRepository.findByUserId(userId)
                .orElseGet(() -> NotificationState.builder()
                        .userId(userId)
                        .latestReadEventLogId(DEFAULT_READ_EVENT_LOG_ID)
                        .build());
        state.setLatestReadEventLogId(latestEventLogId);
        state.setUpdateTime(LocalDateTime.now());
        notificationStateRepository.save(state);
        return getSummary(userId, limit);
    }

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Collections.singletonMap("status", "ok")));
            heartbeatTasks.put(emitter, heartbeatExecutor.scheduleAtFixedRate(
                    () -> sendHeartbeat(userId, emitter),
                    20,
                    20,
                    TimeUnit.SECONDS));
        } catch (IOException ex) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    public void publish(EventLog eventLog) {
        if (eventLog == null || eventLog.getId() == null) {
            return;
        }
        emitters.forEach((userId, userEmitters) -> {
            long latestReadEventLogId = getLatestReadEventLogId(userId);
            boolean read = eventLog.getId() <= latestReadEventLogId
                    || notificationReadRepository.existsByUserIdAndEventLogId(userId, eventLog.getId());
            NotificationVO notification = toNotification(eventLog, read);
            userEmitters.removeIf(emitter -> !sendNotification(emitter, notification));
        });
    }

    private boolean sendNotification(SseEmitter emitter, NotificationVO notification) {
        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .id(String.valueOf(notification.getEventLogId()))
                    .data(notification));
            return true;
        } catch (IOException | IllegalStateException ex) {
            return false;
        }
    }

    private void sendHeartbeat(Long userId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data(Collections.singletonMap("time", System.currentTimeMillis())));
        } catch (IOException | IllegalStateException ex) {
            removeEmitter(userId, emitter);
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        ScheduledFuture<?> heartbeatTask = heartbeatTasks.remove(emitter);
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
        }
        Set<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    private long getLatestReadEventLogId(Long userId) {
        return notificationStateRepository.findByUserId(userId)
                .map(NotificationState::getLatestReadEventLogId)
                .orElse(DEFAULT_READ_EVENT_LOG_ID);
    }

    private Set<Long> findReadEventLogIds(Long userId, Collection<Long> eventLogIds) {
        if (eventLogIds == null || eventLogIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(notificationReadRepository.findReadEventLogIds(userId, eventLogIds));
    }

    private boolean isRead(Long eventLogId, long latestReadEventLogId, Set<Long> individuallyReadEventLogIds) {
        return eventLogId <= latestReadEventLogId || individuallyReadEventLogIds.contains(eventLogId);
    }

    private NotificationVO toNotification(EventLog log, boolean read) {
        String description = hasText(log.getDescription()) ? log.getDescription() : "操作记录";
        String username = hasText(log.getUsername()) ? log.getUsername() : "系统用户";
        String content = username + " 执行了 " + log.getHttpMethod() + " " + log.getUri();
        return NotificationVO.builder()
                .id(log.getId())
                .eventLogId(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .httpMethod(log.getHttpMethod())
                .uri(log.getUri())
                .description(log.getDescription())
                .title(description)
                .content(content)
                .createTime(log.getCreateTime())
                .read(read)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
