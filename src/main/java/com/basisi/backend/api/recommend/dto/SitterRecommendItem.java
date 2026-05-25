package com.basisi.backend.api.recommend.dto;

import com.basisi.backend.api.sitter.dto.SitterSearchResponse;

import java.util.List;

/**
 * 추천 카드 단일 항목입니다.
 *
 * - rank: 1부터 시작하는 순위.
 * - matchScore: 0~100% 매칭 점수. 활성화 조건 대비 일치한 조건 비율(차선책일 때는 불꽃 점수 기반 추정치).
 * - matchedConditions: 일치한 조건 개수(0~5).
 * - totalConditions: 부모가 활성화한 조건 개수(0~5). 0이면 차선책(fallback) 모드.
 * - reasons: 화면에 노출할 한국어 사유 1~3줄.
 * - sitter: 기존 탐색에서 사용하는 SitterSearchResponse 그대로 재사용(프론트 카드 통일).
 */
public record SitterRecommendItem(
        Integer rank,
        Integer matchScore,
        Integer matchedConditions,
        Integer totalConditions,
        List<String> reasons,
        SitterSearchResponse sitter
) {
}
