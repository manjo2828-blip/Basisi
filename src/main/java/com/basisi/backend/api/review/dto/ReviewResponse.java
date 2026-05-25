package com.basisi.backend.api.review.dto;

public record ReviewResponse(
        Long reviewId,
        Long reservationId,
        Long sitterProfileId,
        Long parentUserId,
        String parentName,
        Integer rating,
        String comment,
        String createdAt
) {
}

