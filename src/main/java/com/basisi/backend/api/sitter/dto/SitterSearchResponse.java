package com.basisi.backend.api.sitter.dto;

// 시터 탐색 결과 응답 DTO입니다.
public record SitterSearchResponse(
        // 시터 프로필 ID입니다.
        Long sitterProfileId,
        // 시터 사용자 ID입니다.
        Long userId,
        // 시터 표시 이름입니다.
        String name,
        // 시터 소개글입니다.
        String bio,
        // 나이입니다.
        Integer age,
        // 성별입니다. (FEMALE/MALE)
        String gender,
        // 경력(년)입니다.
        Integer yearsOfExperience,
        // 자격증 유무입니다.
        Boolean hasCertificate,
        // 거주 지역입니다.
        String region,
        // 불꽃 점수입니다.
        Integer flameScore,
        // 불꽃 등급입니다.
        String flameGrade,
        // 완료 예약 수입니다.
        Integer completedReservationCount,
        // 평균 평점입니다.
        Double averageRating,
        // 최근 30일 활동 수입니다.
        Integer recentActivityCount
) {
}

