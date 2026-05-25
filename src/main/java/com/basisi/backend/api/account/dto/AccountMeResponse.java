package com.basisi.backend.api.account.dto;

public record AccountMeResponse(
        Long userId,
        String email,
        String name,
        String role,
        // 이메일 변경 등으로 토큰 갱신이 필요할 때 새 토큰을 반환합니다. (그 외에는 null)
        String accessToken
) {
}

