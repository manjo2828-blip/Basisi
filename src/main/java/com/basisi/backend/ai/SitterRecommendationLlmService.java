package com.basisi.backend.ai;

import com.basisi.backend.ai.dto.RecommendLlmItem;
import com.basisi.backend.ai.dto.RecommendLlmResponse;
import com.basisi.backend.api.recommend.dto.RecommendMatchParams;
import com.basisi.backend.api.recommend.dto.SitterRecommendItem;
import com.basisi.backend.api.recommend.dto.SitterRecommendResponse;
import com.basisi.backend.config.AiProperties;
import com.basisi.backend.domain.profile.ParentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** DB 알고리즘 결과를 OpenAI로 재순위·사유 생성합니다. 실패 시 원본 결과를 유지합니다. */
@Service
public class SitterRecommendationLlmService {

    private static final Logger log = LoggerFactory.getLogger(SitterRecommendationLlmService.class);

    private final AiProperties aiProperties;
    private final OpenAiClient openAiClient;
    private final RecommendPromptBuilder promptBuilder;
    private final RecommendLlmCacheService cacheService;
    private final RecommendLlmRateLimiter rateLimiter;

    public SitterRecommendationLlmService(
            AiProperties aiProperties,
            OpenAiClient openAiClient,
            RecommendPromptBuilder promptBuilder,
            RecommendLlmCacheService cacheService,
            RecommendLlmRateLimiter rateLimiter
    ) {
        this.aiProperties = aiProperties;
        this.openAiClient = openAiClient;
        this.promptBuilder = promptBuilder;
        this.cacheService = cacheService;
        this.rateLimiter = rateLimiter;
    }

    public SitterRecommendResponse enhance(
            SitterRecommendResponse algorithmResponse,
            ParentProfile parent,
            RecommendMatchParams params,
            String additionalRequest,
            String userEmail
    ) {
        if (!aiProperties.isConfigured() || algorithmResponse.items() == null || algorithmResponse.items().isEmpty()) {
            return withSource(algorithmResponse, "ALGORITHM", false);
        }

        try {
            String userPrompt = promptBuilder.buildUserPrompt(
                    parent,
                    params,
                    additionalRequest,
                    algorithmResponse.items(),
                    algorithmResponse.items().size()
            );
            String cacheKey = cacheService.buildKey(userEmail, userPrompt);

            RecommendLlmResponse llmResponse = cacheService.get(cacheKey);
            if (llmResponse == null) {
                if (!rateLimiter.tryAcquire(userEmail)) {
                    log.info("[OpenAI] rate limit exceeded for user={}", userEmail);
                    return withSource(algorithmResponse, "ALGORITHM", true);
                }
                Optional<RecommendLlmResponse> generated = openAiClient.generateRecommendation(
                        promptBuilder.buildSystemInstruction(),
                        userPrompt
                );
                if (generated.isEmpty()) {
                    return withSource(algorithmResponse, "ALGORITHM", true);
                }
                llmResponse = generated.get();
                cacheService.put(cacheKey, llmResponse);
            }

            return mergeLlmResult(algorithmResponse, llmResponse);
        } catch (Exception e) {
            log.warn("[OpenAI] enhance failed, falling back to algorithm: {}", e.getMessage());
            return withSource(algorithmResponse, "ALGORITHM", true);
        }
    }

    private SitterRecommendResponse mergeLlmResult(
            SitterRecommendResponse algorithmResponse,
            RecommendLlmResponse llmResponse
    ) {
        if (llmResponse.items() == null || llmResponse.items().isEmpty()) {
            return withSource(algorithmResponse, "ALGORITHM", true);
        }

        Map<Long, SitterRecommendItem> byId = algorithmResponse.items().stream()
                .collect(Collectors.toMap(item -> item.sitter().sitterProfileId(), item -> item, (a, b) -> a));
        Set<Long> allowedIds = new HashSet<>(byId.keySet());
        Set<Integer> usedRanks = new HashSet<>();

        List<SitterRecommendItem> merged = new ArrayList<>();
        for (RecommendLlmItem llmItem : llmResponse.items().stream()
                .sorted(Comparator.comparingInt(item -> item.rank() != null ? item.rank() : Integer.MAX_VALUE))
                .toList()) {
            if (llmItem.sitterProfileId() == null || !allowedIds.contains(llmItem.sitterProfileId())) {
                log.warn("[OpenAI] invalid sitterProfileId from LLM: {}", llmItem.sitterProfileId());
                return withSource(algorithmResponse, "ALGORITHM", true);
            }
            if (llmItem.rank() == null || llmItem.rank() <= 0 || !usedRanks.add(llmItem.rank())) {
                log.warn("[OpenAI] invalid rank from LLM: {}", llmItem.rank());
                return withSource(algorithmResponse, "ALGORITHM", true);
            }

            SitterRecommendItem original = byId.get(llmItem.sitterProfileId());
            List<String> reasons = sanitizeReasons(llmItem.reasons(), original.reasons());
            merged.add(new SitterRecommendItem(
                    llmItem.rank(),
                    original.matchScore(),
                    original.matchedConditions(),
                    original.totalConditions(),
                    reasons,
                    original.sitter()
            ));
        }

        if (merged.isEmpty()) {
            return withSource(algorithmResponse, "ALGORITHM", true);
        }

        String summary = llmResponse.summary() != null && !llmResponse.summary().isBlank()
                ? llmResponse.summary().trim()
                : algorithmResponse.summary();

        return new SitterRecommendResponse(
                summary,
                merged.size(),
                algorithmResponse.matchMode(),
                merged,
                algorithmResponse.disclaimer(),
                "LLM",
                false
        );
    }

    private List<String> sanitizeReasons(List<String> llmReasons, List<String> fallbackReasons) {
        if (llmReasons == null || llmReasons.isEmpty()) {
            return fallbackReasons;
        }
        List<String> cleaned = llmReasons.stream()
                .filter(reason -> reason != null && !reason.isBlank())
                .map(String::trim)
                .limit(2)
                .collect(Collectors.toCollection(ArrayList::new));
        return cleaned.isEmpty() ? fallbackReasons : cleaned;
    }

    private SitterRecommendResponse withSource(SitterRecommendResponse response, String source, boolean llmFallback) {
        return new SitterRecommendResponse(
                response.summary(),
                response.totalCount(),
                response.matchMode(),
                response.items(),
                response.disclaimer(),
                source,
                llmFallback
        );
    }
}
