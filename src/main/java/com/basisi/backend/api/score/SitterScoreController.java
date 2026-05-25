package com.basisi.backend.api.score;

import com.basisi.backend.api.score.dto.SitterScoreResponse;
import com.basisi.backend.service.SitterScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 시터 불꽃 점수 조회 API를 제공합니다.
@RestController
@RequestMapping("/api/sitter-scores")
public class SitterScoreController {

    private final SitterScoreService sitterScoreService;

    public SitterScoreController(SitterScoreService sitterScoreService) {
        this.sitterScoreService = sitterScoreService;
    }

    @GetMapping("/sitters/{sitterProfileId}")
    public ResponseEntity<SitterScoreResponse> getSitterScore(@PathVariable Long sitterProfileId) {
        return ResponseEntity.ok(sitterScoreService.getBySitterProfileId(sitterProfileId));
    }

    @GetMapping("/me")
    public ResponseEntity<SitterScoreResponse> getMyScore() {
        return ResponseEntity.ok(sitterScoreService.getMySitterScore());
    }
}
