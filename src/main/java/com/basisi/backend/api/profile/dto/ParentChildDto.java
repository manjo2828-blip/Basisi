package com.basisi.backend.api.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** 부모 프로필에 등록되는 아이 한 명분 정보입니다. */
public record ParentChildDto(
        @NotBlank(message = "아이 식별자는 필수입니다.")
        @Size(max = 64, message = "아이 식별자는 64자 이하여야 합니다.")
        String id,
        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,
        @Size(max = 16, message = "성별 값이 너무 깁니다.")
        String gender
) {
}
