package com.xyehyin.hexuanning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习会话实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "practice_session")
@EntityListeners(AuditingEntityListener.class)
public class PracticeSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Integer attempt; // 第几次尝试

    @Column
    private Double totalScore; // 总得分

    @Column
    private Double correctRate; // 正确率

    @Column(nullable = false)
    @Builder.Default
    private Boolean submitted = false; // 是否已提交

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime submitTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "practiceSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StudentAnswer> answers;
} 