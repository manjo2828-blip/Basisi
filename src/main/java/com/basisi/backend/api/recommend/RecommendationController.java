package com.basisi.backend.api.recommend;

import com.basisi.backend.api.recommend.dto.SitterRecommendRequest;
import com.basisi.backend.api.recommend.dto.SitterRecommendResponse;
import com.basisi.backend.service.SitterRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 시터 추천 API 컨트롤러입니다. (PARENT 계정 전용) */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final SitterRecommendationService recommendationService;

    public RecommendationController(SitterRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * 현재 로그인한 부모의 마이페이지 필수 조건을 기준으로 시터를 추천합니다.
     * 매칭 후보가 0건이면 자동으로 불꽃 점수 상위 N명을 차선책으로 반환합니다.
     */
    @PostMapping("/sitters")
    public ResponseEntity<SitterRecommendResponse> recommendSitters(
            @RequestBody(required = false) @Valid SitterRecommendRequest request
    ) {
        SitterRecommendResponse response = recommendationService.recommendForCurrentParent(request);
        return ResponseEntity.ok(response);
    }
}
