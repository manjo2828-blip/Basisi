package com.basisi.backend.api.profile.dto;

/** 시터 프로필 이미지 업로드 결과입니다. */
public record SitterImageUploadResponse(
        String id,
        String url
) {
}
