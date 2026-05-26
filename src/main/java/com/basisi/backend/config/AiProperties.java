package com.basisi.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI LLM 연동 설정입니다.
 * API 키는 {@code BASISI_OPENAI_API_KEY} 환경변수로 주입합니다.
 */
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private boolean enabled = true;
    private String provider = "openai";
    private String apiKey = "";
    private String model = "gpt-4o-mini";
    private String baseUrl = "https://api.openai.com/v1";
    private int timeoutMs = 15_000;
    private int maxRetries = 0;
    private int cacheTtlMinutes = 30;
    private int rateLimitPerUserPerMinute = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }

    public void setCacheTtlMinutes(int cacheTtlMinutes) {
        this.cacheTtlMinutes = cacheTtlMinutes;
    }

    public int getRateLimitPerUserPerMinute() {
        return rateLimitPerUserPerMinute;
    }

    public void setRateLimitPerUserPerMinute(int rateLimitPerUserPerMinute) {
        this.rateLimitPerUserPerMinute = rateLimitPerUserPerMinute;
    }

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
