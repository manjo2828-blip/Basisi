package com.basisi.backend.domain.score;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 시터 점수 저장/조회 리포지토리입니다.
public interface SitterScoreRepository extends JpaRepository<SitterScore, Long> {

    Optional<SitterScore> findBySitterProfileId(Long sitterProfileId);
}
