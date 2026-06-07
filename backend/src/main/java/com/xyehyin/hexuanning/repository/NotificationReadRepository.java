package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * 单条通知已读记录仓库。
 */
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {
    boolean existsByUserIdAndEventLogId(Long userId, Long eventLogId);

    @Query("select nr.eventLogId from NotificationRead nr where nr.userId = :userId and nr.eventLogId in :eventLogIds")
    List<Long> findReadEventLogIds(
            @Param("userId") Long userId,
            @Param("eventLogIds") Collection<Long> eventLogIds
    );
}
