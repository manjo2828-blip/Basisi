package com.basisi.backend.service;

// 공개 상세 응답 DTO import입니다.
import com.basisi.backend.api.sitter.dto.SitterPublicDetailResponse;
import com.basisi.backend.api.score.dto.SitterScoreResponse;
// 탐색 응답 DTO import입니다.
import com.basisi.backend.api.sitter.dto.SitterSearchResponse;
// 시터 프로필 엔티티 import입니다.
import com.basisi.backend.domain.profile.SitterProfile;
// 시터 프로필 리포지토리 import입니다.
import com.basisi.backend.domain.profile.SitterProfileRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
// 서비스 어노테이션입니다.
import org.springframework.stereotype.Service;
// 읽기 전용 트랜잭션 어노테이션입니다.
import org.springframework.transaction.annotation.Transactional;

// 시터 탐색(위치/필터) 비즈니스 로직을 담당하는 서비스입니다.
@Service
public class SitterSearchService {

    // 시터 프로필 리포지토리입니다.
    private final SitterProfileRepository sitterProfileRepository;
    // 시터 점수 서비스입니다.
    private final SitterScoreService sitterScoreService;

    // 생성자로 리포지토리를 주입받습니다.
    public SitterSearchService(
            SitterProfileRepository sitterProfileRepository,
            SitterScoreService sitterScoreService
    ) {
        // 주입받은 리포지토리를 필드에 저장합니다.
        this.sitterProfileRepository = sitterProfileRepository;
        this.sitterScoreService = sitterScoreService;
    }

    // 지역/성별/경력 조건으로 시터를 탐색합니다.
    @Transactional
    public Page<SitterSearchResponse> search(
            String region,
            String gender,
            Integer minYearsOfExperience,
            Pageable pageable
    ) {
        // QueryDSL 기반 DB 동적 쿼리로 탐색 + 페이징을 수행합니다.
        Page<SitterSearchResponse> page = sitterProfileRepository.searchSitters(
                region,
                gender,
                minYearsOfExperience,
                pageable
        );

        // 점수 미집계 상태에서도 목록에 점수가 보이도록 조회 시 보강합니다.
        List<SitterSearchResponse> enriched = page.getContent().stream()
                .map(item -> {
                    var score = sitterScoreService.getBySitterProfileId(item.sitterProfileId());
                    return new SitterSearchResponse(
                            item.sitterProfileId(),
                            item.userId(),
                            item.name(),
                            item.bio(),
                            item.age(),
                            item.gender(),
                            item.yearsOfExperience(),
                            item.hasCertificate(),
                            item.region(),
                            score.score(),
                            score.grade(),
                            score.completedReservationCount(),
                            score.averageRating(),
                            score.recentActivityCount()
                    );
                })
                .toList();

        return new PageImpl<>(enriched, pageable, page.getTotalElements());
    }

    // (legacy) 기존 List 응답은 간단 필터 기반으로 유지합니다.
    @Transactional(readOnly = true)
    public List<SitterSearchResponse> searchLegacy(String region, String gender, Integer minYearsOfExperience) {
        return search(region, gender, minYearsOfExperience, Pageable.ofSize(1000)).getContent();
    }

    // 공개 시터 상세 정보를 조회합니다. (민감 정보 제외)
    @Transactional(readOnly = true)
    public SitterPublicDetailResponse getPublicSitterDetail(Long sitterProfileId) {
        // 시터 프로필을 조회합니다.
        SitterProfile profile = sitterProfileRepository.findById(sitterProfileId)
                // 프로필이 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필을 찾을 수 없습니다."));
        SitterScoreResponse score = sitterScoreService.getBySitterProfileId(sitterProfileId);
        // 공개 응답 DTO를 생성해 반환합니다.
        return new SitterPublicDetailResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getAge(),
                profile.getGender(),
                profile.getBio(),
                profile.getYearsOfExperience(),
                profile.getHasCertificate(),
                profile.getRegion(),
                score.score(),
                score.grade(),
                score.completedReservationCount(),
                score.averageRating(),
                score.reviewCount(),
                score.recentActivityCount(),
                score.averageResponseMinutes(),
                score.weeklyDelta()
        );
    }
}

