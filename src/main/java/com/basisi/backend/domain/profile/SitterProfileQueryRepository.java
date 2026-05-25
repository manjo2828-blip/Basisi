package com.basisi.backend.domain.profile;

import com.basisi.backend.api.recommend.dto.RecommendMatchParams;
import com.basisi.backend.api.recommend.dto.SitterRecommendRow;
import com.basisi.backend.api.sitter.dto.SitterSearchResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 시터 탐색 동적 조건 조회를 위한 커스텀 리포지토리 인터페이스입니다.
public interface SitterProfileQueryRepository {

    // QueryDSL 기반으로 시터 검색 + 페이징을 수행합니다.
    Page<SitterSearchResponse> searchSitters(
            String region,
            String gender,
            Integer minYearsOfExperience,
            Pageable pageable
    );

    /**
     * 부모의 5가지 필수 조건과 시터를 CASE WHEN 합산으로 매칭한 뒤
     * matchedCount >= minMatched 인 후보만 추려 상위 limit 개를 반환합니다.
     *
     * - WHERE 절에서 matchedCount >= minMatched 로 풀스캔 결과를 거름
     * - ORDER BY matchedCount DESC, flameScore DESC 로 DB가 정렬까지 처리
     * - LIMIT 으로 메모리 부담 최소화
     *
     * @param params       부모 필수 조건 파라미터
     * @param minMatched   포함 임계값(matched 가 이 값 이상이어야 결과에 포함)
     * @param limit        결과 최대 개수
     */
    List<SitterRecommendRow> findRecommendCandidates(RecommendMatchParams params, int minMatched, int limit);

    /**
     * 차선책(Fallback): 매칭 후보가 0건일 때 사용합니다.
     * 시터 점수(flameScore) 내림차순으로 상위 limit 명을 반환합니다.
     */
    List<SitterRecommendRow> findTopByFlameScore(int limit);
}
