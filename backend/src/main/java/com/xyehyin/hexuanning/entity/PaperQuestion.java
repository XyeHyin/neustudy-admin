package com.xyehyin.hexuanning.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 试卷题目关联实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paper_question")
public class PaperQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer orderNum; // 题目在试卷中的顺序

    @Column(nullable = false)
    private Integer score; // 该题目在试卷中的分值
} 