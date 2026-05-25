package com.basisi.backend.async;

import com.basisi.backend.domain.reservation.Reservation;
import com.basisi.backend.domain.review.Review;

// 도메인 이벤트 발행 인터페이스입니다.
public interface AsyncEventPublisher {

    void publishReservationStatusChanged(String eventType, Reservation reservation);

    void publishReviewCreated(Review review);
}
