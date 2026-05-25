package com.basisi.backend.async;

import com.basisi.backend.domain.reservation.Reservation;
import com.basisi.backend.domain.review.Review;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// 비동기 이벤트 기능이 비활성화된 환경에서 사용하는 no-op 구현입니다.
@Component
@ConditionalOnProperty(name = "async.events.enabled", havingValue = "false", matchIfMissing = true)
public class NoopAsyncEventPublisher implements AsyncEventPublisher {

    @Override
    public void publishReservationStatusChanged(String eventType, Reservation reservation) {
        // no-op
    }

    @Override
    public void publishReviewCreated(Review review) {
        // no-op
    }
}
