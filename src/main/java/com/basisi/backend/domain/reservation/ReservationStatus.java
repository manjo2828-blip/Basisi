package com.basisi.backend.domain.reservation;

// 예약 상태를 정의하는 열거형입니다.
public enum ReservationStatus {
    // 부모가 예약을 신청한 상태입니다.
    REQUESTED,
    // 시터가 예약을 수락한 상태입니다.
    ACCEPTED,
    // 시터가 예약을 거절한 상태입니다.
    REJECTED,
    // 부모가 예약을 취소한 상태입니다.
    CANCELED
}

