package com.xyehyin.hexuanning.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "question")
@EntityListeners(AuditingEntityListener.class)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title; // 题目标题

    @Column(columnDefinition = "TEXT")
    private String content; // 题目内容（支持富文本）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type; // 题目类型

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty; // 题目难度

    @Column(columnDefinition = "TEXT")
    private String options; // 选项（JSON格式存储，适用于选择题）

    @Column(columnDefinition = "TEXT")
    private String answer; // 正确答案

    @Column(length = 1000)
    private String explanation; // 题目解析

    @Column
    @Builder.Default
    private Integer score = 1; // 题目分值

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // 是否启用

    @Column(name = "is_ai_generated", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isAiGenerated = false; // 是否为AI生成

    @Column(length = 200)
    private String tags; // 标签（多个用逗号分隔）

    @ManyToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinColumn(name = "knowledge_point_id", nullable = false)
    private KnowledgePoint knowledgePoint; // 关联知识点

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    /**
     * 题目类型枚举
     */
    public enum QuestionType {
        SINGLE_CHOICE("单选题"),
        MULTIPLE_CHOICE("多选题"),
        FILL_BLANK("填空题"),
        SHORT_ANSWER("简答题"),
        TRUE_FALSE("判断题"),
        ESSAY("论述题");

        private final String description;

        QuestionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 题目难度枚举
     */
    public enum Difficulty {
        EASY("简单"),
        MEDIUM("中等"),
        HARD("困难");

        private final String description;

        Difficulty(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}