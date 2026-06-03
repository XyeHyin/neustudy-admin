package com.xyehyin.hexuanning.entity;

import jakarta.persistence.*;
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
 * 课程实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course")
@EntityListeners(AuditingEntityListener.class)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String subject;

    @Column(length = 20)
    private String grade;

    @Column(length = 20)
    private String semester;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 255)
    private String coverImage;

    @Column
    private Integer totalHours;

    @Column
    @Builder.Default
    private Integer completedHours = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    // 添加与知识点的一对多关系，配置级联删除
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<KnowledgePoint> knowledgePoints;

    /**
     * 课程状态枚举
     */
    public enum CourseStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        COMPLETED,  // 已完成
        ARCHIVED    // 已归档
    }
}