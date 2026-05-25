package com.basisi.backend.api.review.dto;

public record ReviewSummaryResponse(
        Long sitterProfileId,
        double averageRating,
        long reviewCount
) {
}

