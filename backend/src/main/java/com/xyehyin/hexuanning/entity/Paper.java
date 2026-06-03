package com.xyehyin.hexuanning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paper")
@EntityListeners(AuditingEntityListener.class)
public class Paper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 试卷标题

    @Column(length = 500)
    private String description; // 试卷描述

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher; // 创建教师

    @Column
    @Builder.Default
    private Integer timeLimit = 60; // 时间限制（分钟）

    @Column(nullable = false)
    @Builder.Default
    private Boolean showAnswer = false; // 是否显示答案

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowRetry = false; // 是否允许重复练习

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QuestionOrderType questionOrderType = QuestionOrderType.FIXED; // 题目顺序

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaperStatus status = PaperStatus.DRAFT; // 试卷状态

    @Column
    private Integer totalScore; // 总分

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaperQuestion> questions;

    /**
     * 题目顺序类型
     */
    public enum QuestionOrderType {
        FIXED,   // 固定顺序
        RANDOM   // 随机顺序
    }

    /**
     * 试卷状态
     */
    public enum PaperStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED    // 已归档
    }
} 