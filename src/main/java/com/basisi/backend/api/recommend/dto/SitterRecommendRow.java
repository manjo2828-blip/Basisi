package com.basisi.backend.api.recommend.dto;

import com.basisi.backend.domain.profile.SitterNationalityType;

/**
 * Repository 계층 전용 프로젝션 DTO입니다.
 *
 * QueryDSL이 CASE WHEN 합산으로 계산한 matchedCount까지 함께 가져와서,
 * Service 계층에서 reasons 생성과 matchScore 산출에 그대로 활용합니다.
 *
 * Sitter 본인 식별 정보 + 도메인 핵심 필드(나이/성별/경력/국적/지역) + 시터 점수 + matchedCount.
 */
public record SitterRecommendRow(
        Long sitterProfileId,
        Long userId,
        String name,
        String bio,
        Integer age,
        String gender,
        Integer yearsOfExperience,
        Boolean hasCertificate,
        String region,
        SitterNationalityType nationalityType,
        Integer flameScore,
        String flameGrade,
        Integer completedReservationCount,
        Double averageRating,
        Integer recentActivityCount,
        Integer matchedCount
) {
}
