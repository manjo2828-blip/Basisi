package com.basisi.backend.api.review;

import com.basisi.backend.api.review.dto.ReviewCreateRequest;
import com.basisi.backend.api.review.dto.ReviewResponse;
import com.basisi.backend.api.review.dto.ReviewSummaryResponse;
import com.basisi.backend.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sitters/{sitterProfileId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsBySitter(@PathVariable Long sitterProfileId) {
        List<ReviewResponse> response = reviewService.getReviewsBySitter(sitterProfileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sitters/{sitterProfileId}/summary")
    public ResponseEntity<ReviewSummaryResponse> getReviewSummaryBySitter(@PathVariable Long sitterProfileId) {
        ReviewSummaryResponse response = reviewService.getReviewSummaryBySitter(sitterProfileId);
        return ResponseEntity.ok(response);
    }
}

