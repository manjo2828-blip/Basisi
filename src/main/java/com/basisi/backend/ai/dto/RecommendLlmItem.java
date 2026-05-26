package com.basisi.backend.ai.dto;

import java.util.List;

/** OpenAI가 반환하는 추천 카드 단일 항목입니다. */
public record RecommendLlmItem(
        Long sitterProfileId,
        Integer rank,
        List<String> reasons
) {
}
