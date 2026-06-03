package com.xyehyin.hexuanning.repository;

import com.xyehyin.hexuanning.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
 
/**
 * 事件日志仓库接口
 */
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
} 