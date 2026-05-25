package com.basisi.backend.service;

import com.basisi.backend.api.score.dto.SitterScoreResponse;
import com.basisi.backend.domain.profile.SitterProfile;
import com.basisi.backend.domain.profile.SitterProfileRepository;
import com.basisi.backend.domain.reservation.Reservation;
import com.basisi.backend.domain.reservation.ReservationRepository;
import com.basisi.backend.domain.reservation.ReservationStatus;
import com.basisi.backend.domain.review.ReviewRepository;
import com.basisi.backend.domain.review.ReviewSummaryRow;
import com.basisi.backend.domain.score.SitterScore;
import com.basisi.backend.domain.score.SitterScoreRepository;
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.domain.user.UserRole;
import com.basisi.backend.security.SecurityUtil;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 시터 불꽃 점수 계산/조회 서비스를 제공합니다.
@Service
public class SitterScoreService {

    private final SitterScoreRepository sitterScoreRepository;
    private final SitterProfileRepository sitterProfileRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public SitterScoreService(
            SitterScoreRepository sitterScoreRepository,
            SitterProfileRepository sitterProfileRepository,
            ReservationRepository reservationRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.sitterScoreRepository = sitterScoreRepository;
        this.sitterProfileRepository = sitterProfileRepository;
        this.reservationRepository = reservationRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SitterScoreResponse recalculateBySitterProfileId(Long sitterProfileId) {
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30Days = now.minusDays(30);
        LocalDateTime last7Days = now.minusDays(7);
        LocalDateTime prev7Days = now.minusDays(14);

        List<Reservation> reservations = reservationRepository.findBySitterProfileIdOrderByCreatedAtDesc(sitterProfileId);
        int completedCount = (int) reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACCEPTED && r.getEndAt() != null && r.getEndAt().isBefore(now))
                .count();

        long recentReservationActivity = reservations.stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isAfter(last30Days))
                .count();

        long weekReservationActivity = reservations.stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isAfter(last7Days))
                .count();
        long prevWeekReservationActivity = reservations.stream()
                .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isAfter(prev7Days) && r.getUpdatedAt().isBefore(last7Days))
                .count();

        // 수락된 예약의 요청~수락 시간 차이를 응답속도 지표로 사용합니다.
        List<Reservation> acceptedReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACCEPTED && r.getCreatedAt() != null && r.getUpdatedAt() != null)
                .toList();

        Double avgResponseMinutes = acceptedReservations.isEmpty()
                ? null
                : acceptedReservations.stream()
                .mapToLong(r -> Math.max(0, ChronoUnit.MINUTES.between(r.getCreatedAt(), r.getUpdatedAt())))
                .average()
                .orElse(0.0);

        int responseBaseScore = avgResponseMinutes == null
                ? 0
                : clamp((int) Math.round(100 - (avgResponseMinutes / 10.0)), 0, 100);

        var reviewSummaryRows = reviewRepository.findSummaryBySitterProfileId(sitterProfileId);
        double averageRating = ReviewSummaryRow.averageRating(reviewSummaryRows);
        int reviewCount = (int) ReviewSummaryRow.reviewCount(reviewSummaryRows);

        long recentReviewActivity = reviewRepository.findBySitterProfileIdOrderByCreatedAtDesc(sitterProfileId).stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(last30Days))
                .count();
        long weekReviewActivity = reviewRepository.findBySitterProfileIdOrderByCreatedAtDesc(sitterProfileId).stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(last7Days))
                .count();
        long prevWeekReviewActivity = reviewRepository.findBySitterProfileIdOrderByCreatedAtDesc(sitterProfileId).stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(prev7Days) && r.getCreatedAt().isBefore(last7Days))
                .count();

        int recentActivityCount = (int) (recentReservationActivity + recentReviewActivity);
        int weeklyDelta = (int) ((weekReservationActivity + weekReviewActivity) - (prevWeekReservationActivity + prevWeekReviewActivity));

        int completedScore = Math.min(completedCount * 4, 40);
        int ratingScore = clamp((int) Math.round((averageRating / 5.0) * 35.0), 0, 35);
        int activityScore = Math.min(recentActivityCount * 2, 15);
        int responseScoreWeighted = clamp((int) Math.round((responseBaseScore / 100.0) * 10.0), 0, 10);
        int totalScore = clamp(completedScore + ratingScore + activityScore + responseScoreWeighted, 0, 100);

        String grade = resolveGrade(totalScore);

        SitterScore score = sitterScoreRepository.findBySitterProfileId(sitterProfileId)
                .orElseGet(() -> new SitterScore(profile.getId()));
        score.updateMetrics(
                totalScore,
                grade,
                completedCount,
                roundOneDecimal(averageRating),
                reviewCount,
                recentActivityCount,
                avgResponseMinutes == null ? null : roundOneDecimal(avgResponseMinutes),
                responseBaseScore,
                weeklyDelta
        );
        SitterScore saved = sitterScoreRepository.save(score);
        return toResponse(saved);
    }

    @Transactional
    public SitterScoreResponse recalculateByReservationId(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        Long sitterProfileId = reservation.getSitterProfile().getId();
        return recalculateBySitterProfileId(sitterProfileId);
    }

    @Transactional
    public SitterScoreResponse getBySitterProfileId(Long sitterProfileId) {
        return sitterScoreRepository.findBySitterProfileId(sitterProfileId)
                .map(this::toResponse)
                .orElseGet(() -> recalculateBySitterProfileId(sitterProfileId));
    }

    @Transactional(readOnly = true)
    public SitterScoreResponse getMySitterScore() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.SITTER) {
            throw new IllegalArgumentException("시터 계정만 점수를 조회할 수 있습니다.");
        }
        SitterProfile profile = sitterProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필이 존재하지 않습니다."));
        return recalculateBySitterProfileId(profile.getId());
    }

    private SitterScoreResponse toResponse(SitterScore score) {
        return new SitterScoreResponse(
                score.getSitterProfileId(),
                score.getScore(),
                score.getGrade(),
                score.getCompletedReservationCount(),
                score.getAverageRating(),
                score.getReviewCount(),
                score.getRecentActivityCount(),
                score.getAverageResponseMinutes(),
                score.getResponseScore(),
                score.getWeeklyDelta()
        );
    }

    private String resolveGrade(int score) {
        if (score >= 85) return "S";
        if (score >= 70) return "A";
        if (score >= 55) return "B";
        if (score >= 40) return "C";
        return "D";
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
