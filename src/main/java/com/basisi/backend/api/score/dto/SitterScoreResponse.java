package com.basisi.backend.api.score.dto;

// 시터 불꽃 점수 조회 응답 DTO입니다.
public record SitterScoreResponse(
        Long sitterProfileId,
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
}
