package com.basisi.backend.api.auth.dto;

// 이메일 형식 검증 어노테이션입니다.
import jakarta.validation.constraints.Email;
// 빈 값 검증 어노테이션입니다.
import jakarta.validation.constraints.NotBlank;

// 로그인 요청 바디를 표현하는 DTO입니다.
public record LoginRequest(
        // 이메일 입력값을 검증합니다.
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        // 비밀번호 입력값을 검증합니다.
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
