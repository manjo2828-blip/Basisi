package com.basisi.backend.ai.dto;

import java.util.List;

/** OpenAI가 반환하는 추천 JSON 스키마입니다. */
public record RecommendLlmResponse(
        String summary,
        List<RecommendLlmItem> items
) {
}
