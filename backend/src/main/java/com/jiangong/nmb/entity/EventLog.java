package com.jiangong.nmb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 事件日志实体，用于记录用户操作事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_log")
public class EventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    @Column(name = "uri", length = 200, nullable = false)
    private String uri;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
} 