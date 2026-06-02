package com.jiangong.nmb.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 学生答案实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student_answer")
public class StudentAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_session_id", nullable = false)
    private PracticeSession practiceSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String answerContent; // 学生的具体答案

    @Column
    private Double score; // 得分

    @Column
    private Boolean correct; // 是否正确

    @Column(length = 1000)
    private String aiComment; // AI评语

    @Column
    private Boolean marked; // 题目是否标记
} 