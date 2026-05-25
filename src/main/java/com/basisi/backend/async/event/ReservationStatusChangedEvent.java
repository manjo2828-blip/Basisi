package com.basisi.backend.async.event;

import java.time.LocalDateTime;

// 예약 상태 변경 이벤트 스키마입니다.
public record ReservationStatusChangedEvent(
        String eventId,
        LocalDateTime occurredAt,
        String eventType,
        Long reservationId,
        Long parentUserId,
        Long sitterUserId,
        String status,
        LocalDateTime reservationStartAt,
        LocalDateTime reservationEndAt
) {
}
