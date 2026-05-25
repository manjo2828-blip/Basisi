package com.basisi.backend.api.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 시터 활동 희망 지역 한 곳입니다. */
public record PreferredRegionDto(
        @NotBlank(message = "시·도는 필수입니다.")
        @Size(max = 50, message = "시·도는 50자 이하여야 합니다.")
        String sido,
        @NotBlank(message = "시·군·구는 필수입니다.")
        @Size(max = 80, message = "시·군·구는 80자 이하여야 합니다.")
        String sigungu,
        @NotBlank(message = "동·읍·면은 필수입니다.")
        @Size(max = 80, message = "동·읍·면은 80자 이하여야 합니다.")
        String dong
) {
}
