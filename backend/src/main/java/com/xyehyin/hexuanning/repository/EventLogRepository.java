package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
 
/**
 * 事件日志仓库接口
 */
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    long countByIdGreaterThan(Long id);

    Optional<EventLog> findTopByOrderByIdDesc();

    @Query("""
            select count(e) from EventLog e
            where e.id > :latestReadEventLogId
              and not exists (
                select nr.id from NotificationRead nr
                where nr.userId = :userId and nr.eventLogId = e.id
              )
            """)
    long countUnreadNotifications(
            @Param("userId") Long userId,
            @Param("latestReadEventLogId") Long latestReadEventLogId
    );
} 
