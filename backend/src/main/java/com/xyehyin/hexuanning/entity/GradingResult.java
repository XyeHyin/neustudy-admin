package com.xyehyin.hexuanning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 判分结果实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "grading_result")
public class GradingResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_answer_id", nullable = false)
    private StudentAnswer studentAnswer;

    // AI判分
    @Column
    private Double aiScore;
    @Column(length = 1000)
    private String aiComment;
    @Column(length = 1000)
    private String aiReason;
    @Column
    private LocalDateTime aiGradingTime;

    // 人工复核
    @Column
    private Double finalScore;
    @Column(length = 1000)
    private String teacherComment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_teacher_id")
    private User reviewTeacher;
    @Column
    private LocalDateTime reviewTime;
} 