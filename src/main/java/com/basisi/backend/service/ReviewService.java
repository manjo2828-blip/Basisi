package com.basisi.backend.service;

import com.basisi.backend.async.AsyncEventPublisher;
import com.basisi.backend.api.review.dto.ReviewCreateRequest;
import com.basisi.backend.api.review.dto.ReviewResponse;
import com.basisi.backend.api.review.dto.ReviewSummaryResponse;
import com.basisi.backend.domain.reservation.Reservation;
import com.basisi.backend.domain.reservation.ReservationRepository;
import com.basisi.backend.domain.reservation.ReservationStatus;
import com.basisi.backend.domain.review.Review;
import com.basisi.backend.domain.review.ReviewRepository;
import com.basisi.backend.domain.review.ReviewSummaryRow;
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.domain.user.UserRole;
import com.basisi.backend.security.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final AsyncEventPublisher asyncEventPublisher;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            AsyncEventPublisher asyncEventPublisher
    ) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.asyncEventPublisher = asyncEventPublisher;
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.PARENT) {
            throw new IllegalArgumentException("부모 계정만 리뷰를 작성할 수 있습니다.");
        }

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        Long ownerParentUserId = reservation.getParentProfile().getUser().getId();
        if (!ownerParentUserId.equals(user.getId())) {
            throw new IllegalArgumentException("본인이 신청한 예약에만 리뷰를 작성할 수 있습니다.");
        }

        if (reservation.getStatus() != ReservationStatus.ACCEPTED) {
            throw new IllegalArgumentException("수락된 예약에만 리뷰를 작성할 수 있습니다.");
        }
        if (reservation.getEndAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약 종료 이후에 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByReservationId(reservation.getId())) {
            throw new IllegalArgumentException("해당 예약에는 이미 리뷰가 작성되었습니다.");
        }

        String normalizedComment = StringUtils.hasText(request.comment()) ? request.comment().trim() : null;
        Review review = new Review(reservation, request.rating(), normalizedComment);
        Review saved = reviewRepository.save(review);
        runAfterCommit(() -> asyncEventPublisher.publishReviewCreated(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsBySitter(Long sitterProfileId) {
        return reviewRepository.findBySitterProfileIdOrderByCreatedAtDesc(sitterProfileId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummaryBySitter(Long sitterProfileId) {
        var summaryRows = reviewRepository.findSummaryBySitterProfileId(sitterProfileId);
        double average = ReviewSummaryRow.averageRating(summaryRows);
        long count = ReviewSummaryRow.reviewCount(summaryRows);
        return new ReviewSummaryResponse(sitterProfileId, roundOneDecimal(average), count);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private ReviewResponse toResponse(Review review) {
        Reservation reservation = review.getReservation();
        return new ReviewResponse(
                review.getId(),
                reservation.getId(),
                review.getSitterProfileId(),
                reservation.getParentProfile().getUser().getId(),
                reservation.getParentProfile().getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt().toString()
        );
    }

    private void runAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }
}

