package com.jiangong.nmb.repository;

import com.jiangong.nmb.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
 
/**
 * 事件日志仓库接口
 */
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
} 