package com.basisi.backend.domain.score;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 시터 불꽃 점수 집계 정보를 저장하는 엔티티입니다.
@Entity
@Table(name = "sitter_scores")
@Getter
@NoArgsConstructor
public class SitterScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sitter_profile_id", nullable = false, unique = true)
    private Long sitterProfileId;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 10)
    private String grade;

    @Column(name = "completed_reservation_count", nullable = false)
    private Integer completedReservationCount;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Column(name = "recent_activity_count", nullable = false)
    private Integer recentActivityCount;

    @Column(name = "average_response_minutes", nullable = true)
    private Double averageResponseMinutes;

    @Column(name = "response_score", nullable = false)
    private Integer responseScore;

    @Column(name = "weekly_delta", nullable = false)
    private Integer weeklyDelta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SitterScore(Long sitterProfileId) {
        this.sitterProfileId = sitterProfileId;
        this.score = 0;
        this.grade = "NEW";
        this.completedReservationCount = 0;
        this.averageRating = 0.0;
        this.reviewCount = 0;
        this.recentActivityCount = 0;
        this.averageResponseMinutes = null;
        this.responseScore = 0;
        this.weeklyDelta = 0;
    }

    public void updateMetrics(
            Integer score,
            String grade,
            Integer completedReservationCount,
            Double averageRating,
            Integer reviewCount,
            Integer recentActivityCount,
            Double averageResponseMinutes,
            Integer responseScore,
            Integer weeklyDelta
    ) {
        this.score = score;
        this.grade = grade;
        this.completedReservationCount = completedReservationCount;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.recentActivityCount = recentActivityCount;
        this.averageResponseMinutes = averageResponseMinutes;
        this.responseScore = responseScore;
        this.weeklyDelta = weeklyDelta;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
