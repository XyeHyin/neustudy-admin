package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.NotificationState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户通知读取状态仓库。
 */
public interface NotificationStateRepository extends JpaRepository<NotificationState, Long> {
    Optional<NotificationState> findByUserId(Long userId);
}
