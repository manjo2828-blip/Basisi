package com.basisi.backend.api.auth.dto;

// 로그인/회원가입 응답으로 사용할 DTO입니다.
public record AuthResponse(
        // 인증에 성공한 사용자 ID입니다.
        Long userId,
        // 인증에 성공한 사용자 이메일입니다.
        String email,
        // 인증에 성공한 사용자 표시 이름입니다.
        String name,
        // 인증에 성공한 사용자 역할입니다.
        String role,
        // 클라이언트가 사용할 Access Token 문자열입니다.
        String accessToken
) {
}
