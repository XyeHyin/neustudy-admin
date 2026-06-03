package com.xyehyin.hexuanning.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识点实体类
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_point")
@EntityListeners(AuditingEntityListener.class)
public class KnowledgePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 知识点名称

    @Column(length = 500)
    private String description; // 知识点描述

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 所属课程

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM; // 难易程度

    @Column
    private Integer orderNum; // 排序号

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // 是否启用

    @Column(length = 1000)
    private String content; // 知识点内容

    @Column(length = 200)
    private String keywords; // 关键词（多个用逗号分隔）

    // 添加与题目的一对多关系，配置级联删除
    @OneToMany(mappedBy = "knowledgePoint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    /**
     * 难易程度枚举
     */
    public enum Difficulty {
        EASY("简单"),       // 简单
        MEDIUM("中等"),     // 中等
        HARD("困难");       // 困难

        private final String description;

        Difficulty(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}