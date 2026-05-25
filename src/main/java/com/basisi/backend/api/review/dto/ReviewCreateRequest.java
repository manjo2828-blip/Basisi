package com.basisi.backend.api.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull(message = "reservationId는 필수입니다.")
        Long reservationId,

        @NotNull(message = "rating은 필수입니다.")
        @Min(value = 1, message = "rating은 1~5 사이여야 합니다.")
        @Max(value = 5, message = "rating은 1~5 사이여야 합니다.")
        Integer rating,

        @Size(max = 1000, message = "comment는 1000자 이하여야 합니다.")
        String comment
) {
}

