package com.basisi.backend.ai;

import com.basisi.backend.ai.dto.RecommendLlmResponse;
import com.basisi.backend.config.AiProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

/** 동일 입력에 대한 OpenAI 호출 결과를 메모리에 캐싱합니다. */
@Component
public class RecommendLlmCacheService {

    private record CacheEntry(RecommendLlmResponse value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final AiProperties aiProperties;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public RecommendLlmCacheService(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    public RecommendLlmResponse get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(cacheKey, entry);
            return null;
        }
        return entry.value();
    }

    public void put(String cacheKey, RecommendLlmResponse value) {
        int ttlMinutes = Math.max(1, aiProperties.getCacheTtlMinutes());
        cache.put(cacheKey, new CacheEntry(value, Instant.now().plusSeconds(ttlMinutes * 60L)));
    }

    public String buildKey(String userEmail, String payloadJson) {
        return sha256(userEmail + "|" + payloadJson);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
