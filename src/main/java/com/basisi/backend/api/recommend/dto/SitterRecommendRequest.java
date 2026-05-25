package com.basisi.backend.api.recommend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * AI 시터 추천 요청 DTO입니다.
 *
 * - useMyProfile: 부모 마이페이지의 필수 조건을 사용할지 여부(현재 단계에선 항상 true 기준).
 * - additionalRequest: 향후 LLM 단계 확장을 위한 자유 텍스트(현 알고리즘에서는 미사용).
 * - limit: 결과 카드 개수 (기본 5, 3~10 사이로 강제).
 */
public record SitterRecommendRequest(
        Boolean useMyProfile,
        @Size(max = 1000, message = "추가 요청은 1000자 이하여야 합니다.")
        String additionalRequest,
        @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
        @Max(value = 20, message = "limit은 20 이하여야 합니다.")
        Integer limit
) {
}
