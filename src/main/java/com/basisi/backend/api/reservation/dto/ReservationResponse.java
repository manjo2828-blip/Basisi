package com.basisi.backend.api.reservation.dto;

// 예약 조회 응답 DTO입니다.
public record ReservationResponse(
        // 예약 ID입니다.
        Long reservationId,
        // 예약 상태입니다.
        String status,
        // 시작 시각 문자열입니다.
        String startAt,
        // 종료 시각 문자열입니다.
        String endAt,
        // 요청 메모입니다.
        String note,
        // 부모 사용자 ID입니다.
        Long parentUserId,
        // 부모 사용자 이름입니다.
        String parentName,
        // 시터 프로필 ID입니다.
        Long sitterProfileId,
        // 시터 사용자 ID입니다.
        Long sitterUserId,
        // 시터 사용자 이름입니다.
        String sitterName,
        // 리뷰 작성 여부입니다.
        Boolean reviewed
) {
}

