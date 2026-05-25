package com.basisi.backend.api.auth.dto;

// 사용자 역할 enum을 참조하기 위한 import입니다.
import com.basisi.backend.domain.user.UserRole;
// 이메일 형식 검증 어노테이션입니다.
import jakarta.validation.constraints.Email;
// 빈 값 검증 어노테이션입니다.
import jakarta.validation.constraints.NotBlank;
// 최소 길이 검증 어노테이션입니다.
import jakarta.validation.constraints.Size;

// 회원가입 요청 바디를 표현하는 DTO입니다.
public record SignUpRequest(
        // 이메일 입력값을 검증합니다.
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        // 비밀번호 최소 길이를 검증합니다.
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,
        // 이름 입력값을 검증합니다.
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        // 사용자 역할 입력값을 받습니다.
        UserRole role
) {
}
