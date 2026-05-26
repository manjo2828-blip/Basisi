package com.basisi.backend.api.recommend.dto;

import java.util.List;

/**
 * AI 시터 추천 응답 DTO입니다.
 *
 * - summary: 화면 헤더 멘트.
 * - totalCount: 반환된 추천 카드 수.
 * - matchMode: "PERFECT_MATCH" (1순위 교집합) 또는 "FALLBACK_TOP_SCORE" (2순위 차선책).
 * - items: 추천 카드 목록.
 * - disclaimer: 안내 문구.
 * - source: "LLM" 또는 "ALGORITHM".
 * - llmFallback: OpenAI 호출을 시도했으나 실패해 알고리즘 결과를 사용한 경우 true.
 */
public record SitterRecommendResponse(
        String summary,
        Integer totalCount,
        String matchMode,
        List<SitterRecommendItem> items,
        String disclaimer,
        String source,
        Boolean llmFallback
) {
}
