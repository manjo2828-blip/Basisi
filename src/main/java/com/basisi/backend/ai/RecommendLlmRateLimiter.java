package com.basisi.backend.ai;

import com.basisi.backend.config.AiProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** 사용자별 OpenAI 호출 빈도를 제한합니다. */
@Component
public class RecommendLlmRateLimiter {

    private final AiProperties aiProperties;
    private final ConcurrentHashMap<String, List<Long>> timestampsByUser = new ConcurrentHashMap<>();

    public RecommendLlmRateLimiter(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    public boolean tryAcquire(String userKey) {
        int limit = Math.max(1, aiProperties.getRateLimitPerUserPerMinute());
        long windowStart = Instant.now().minusSeconds(60).toEpochMilli();
        List<Long> timestamps = timestampsByUser.computeIfAbsent(userKey, key -> new ArrayList<>());
        synchronized (timestamps) {
            timestamps.removeIf(ts -> ts < windowStart);
            if (timestamps.size() >= limit) {
                return false;
            }
            timestamps.add(Instant.now().toEpochMilli());
            return true;
        }
    }
}
