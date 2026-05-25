package com.basisi.backend.api.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String currentPassword,
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 6, max = 72, message = "새 비밀번호는 6~72자여야 합니다.")
        String newPassword
) {
}

