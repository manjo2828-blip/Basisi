package com.basisi.backend.ai;

import com.basisi.backend.ai.dto.RecommendLlmResponse;
import com.basisi.backend.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** OpenAI Chat Completions API 클라이언트입니다. */
@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final WebClient openAiWebClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public OpenAiClient(WebClient openAiWebClient, AiProperties aiProperties, ObjectMapper objectMapper) {
        this.openAiWebClient = openAiWebClient;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<RecommendLlmResponse> generateRecommendation(String systemInstruction, String userPrompt) {
        if (!aiProperties.isConfigured()) {
            return Optional.empty();
        }

        int attempts = Math.max(1, aiProperties.getMaxRetries() + 1);
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                String rawJson = callOpenAi(systemInstruction, userPrompt);
                RecommendLlmResponse parsed = objectMapper.readValue(rawJson, RecommendLlmResponse.class);
                return Optional.of(parsed);
            } catch (WebClientResponseException e) {
                lastError = e;
                log.warn("[OpenAI] HTTP {} on attempt {}/{}: {}", e.getStatusCode().value(), attempt, attempts, e.getMessage());
                if (!isRetryable(e) || attempt == attempts) {
                    break;
                }
            } catch (Exception e) {
                lastError = e instanceof RuntimeException re ? re : new RuntimeException(e);
                log.warn("[OpenAI] parse/call failed on attempt {}/{}: {}", attempt, attempts, e.getMessage());
                if (attempt == attempts) {
                    break;
                }
            }
        }

        if (lastError != null) {
            log.error("[OpenAI] recommendation call failed after retries", lastError);
        }
        return Optional.empty();
    }

    private String callOpenAi(String systemInstruction, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("temperature", 0.3);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemInstruction),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("response_format", Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "sitter_recommendation",
                        "strict", true,
                        "schema", responseSchema()
                )
        ));

        Map<?, ?> response = openAiWebClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofMillis(aiProperties.getTimeoutMs()));

        return extractContent(response);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            throw new IllegalStateException("OpenAI response is null");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("OpenAI returned no choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null || message.get("content") == null) {
            throw new IllegalStateException("OpenAI choice has no message content");
        }
        return String.valueOf(message.get("content"));
    }

    private Map<String, Object> responseSchema() {
        Map<String, Object> itemSchema = new LinkedHashMap<>();
        itemSchema.put("type", "object");
        itemSchema.put("properties", Map.of(
                "sitterProfileId", Map.of("type", "integer"),
                "rank", Map.of("type", "integer"),
                "reasons", Map.of("type", "array", "items", Map.of("type", "string"))
        ));
        itemSchema.put("required", List.of("sitterProfileId", "rank", "reasons"));
        itemSchema.put("additionalProperties", false);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "object");
        root.put("properties", Map.of(
                "summary", Map.of("type", "string"),
                "items", Map.of("type", "array", "items", itemSchema)
        ));
        root.put("required", List.of("summary", "items"));
        root.put("additionalProperties", false);
        return root;
    }

    private boolean isRetryable(WebClientResponseException e) {
        int code = e.getStatusCode().value();
        return code >= 500;
    }
}
