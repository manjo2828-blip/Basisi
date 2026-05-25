package com.basisi.backend.api.reservation.dto;

// 빈 값 검증 어노테이션입니다.
import jakarta.validation.constraints.NotNull;
// 길이 검증 어노테이션입니다.
import jakarta.validation.constraints.Size;
// 날짜/시간 타입입니다.
import java.time.LocalDateTime;

// 예약 생성(신청) 요청 DTO입니다.
public record ReservationCreateRequest(
        // 예약 대상 시터 프로필 ID입니다.
        @NotNull(message = "시터 프로필 ID는 필수입니다.")
        Long sitterProfileId,
        // 시작 시각입니다.
        @NotNull(message = "시작 시각은 필수입니다.")
        LocalDateTime startAt,
        // 종료 시각입니다.
        @NotNull(message = "종료 시각은 필수입니다.")
        LocalDateTime endAt,
        // 요청 메모입니다.
        @Size(max = 500, message = "요청 메모는 500자 이하여야 합니다.")
        String note
) {
}

