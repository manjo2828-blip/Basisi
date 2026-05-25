package com.basisi.backend.async.event;

import java.time.LocalDateTime;

// 리뷰 생성 이벤트 스키마입니다.
public record ReviewCreatedEvent(
        String eventId,
        LocalDateTime occurredAt,
        String eventType,
        Long reviewId,
        Long reservationId,
        Long sitterProfileId,
        Long parentUserId,
        Integer rating
) {
}
