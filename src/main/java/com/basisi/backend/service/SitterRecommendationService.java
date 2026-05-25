package com.basisi.backend.service;

import com.basisi.backend.api.recommend.dto.RecommendMatchParams;
import com.basisi.backend.api.recommend.dto.SitterRecommendItem;
import com.basisi.backend.api.recommend.dto.SitterRecommendRequest;
import com.basisi.backend.api.recommend.dto.SitterRecommendResponse;
import com.basisi.backend.api.recommend.dto.SitterRecommendRow;
import com.basisi.backend.api.sitter.dto.SitterSearchResponse;
import com.basisi.backend.domain.profile.ParentProfile;
import com.basisi.backend.domain.profile.ParentProfileRepository;
import com.basisi.backend.domain.profile.PreferredSitterAgeRange;
import com.basisi.backend.domain.profile.PreferredSitterExperience;
import com.basisi.backend.domain.profile.SitterNationalityType;
import com.basisi.backend.domain.profile.SitterProfileRepository;
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.domain.user.UserRole;
import com.basisi.backend.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 시터 추천 비즈니스 로직 서비스입니다.
 *
 * 흐름:
 * 1) 부모 마이페이지의 5가지 필수 조건을 평면 파라미터로 정규화
 * 2) QueryDSL CASE WHEN 합산으로 매칭 점수 가장 높은 시터 후보 N명 조회
 * 3) 후보가 0명이면 차선책(불꽃 점수 상위 N명) 조회
 * 4) 각 후보에 매칭률(%)·reasons·rank 부여 후 응답 DTO 빌드
 */
@Service
public class SitterRecommendationService {

    private static final int MIN_LIMIT = 3;
    private static final int MAX_LIMIT = 10;
    private static final int DEFAULT_LIMIT = 5;

    private final UserRepository userRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final SitterProfileRepository sitterProfileRepository;

    public SitterRecommendationService(
            UserRepository userRepository,
            ParentProfileRepository parentProfileRepository,
            SitterProfileRepository sitterProfileRepository
    ) {
        this.userRepository = userRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.sitterProfileRepository = sitterProfileRepository;
    }

    /** 현재 로그인한 부모를 기준으로 추천 결과를 생성합니다. */
    @Transactional(readOnly = true)
    public SitterRecommendResponse recommendForCurrentParent(SitterRecommendRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.PARENT) {
            throw new IllegalArgumentException("부모 계정만 AI 추천을 받을 수 있습니다.");
        }

        // 부모 프로필이 없으면 활성 조건도 없으므로 즉시 차선책 경로로 분기합니다.
        ParentProfile parent = parentProfileRepository.findByUserId(user.getId()).orElse(null);

        int limit = clampLimit(request != null ? request.limit() : null);
        RecommendMatchParams params = buildParams(parent);

        if (params.activeConditionCount() == 0) {
            return buildFallbackResponse(limit, "부모 마이페이지에 필수 조건이 비어 있어, 평점이 높은 인기 시터를 우선 보여드릴게요.");
        }

        int activeCount = params.activeConditionCount();
        // 후보를 limit 의 3배(최소 20명) 가져와 동점자에서 다양성 확보 여지를 둡니다.
        int candidatePool = Math.max(limit * 3, 20);

        // 1차: 활성 조건을 전부 만족하는 시터만(완전 일치)
        List<SitterRecommendRow> rows = sitterProfileRepository.findRecommendCandidates(params, activeCount, candidatePool);
        String matchMode = "PERFECT_MATCH";
        String summary = activeCount == 1
                ? "마이페이지에 등록한 필수 조건을 만족하는 시터를 골라드렸어요."
                : "마이페이지의 필수 조건 " + activeCount + "가지를 모두 만족하는 시터를 골라드렸어요.";

        // 2차: 완전 일치 시터가 한 명도 없으면 한 가지만 빗나간 시터(거의 일치)까지 완화
        if (rows.isEmpty() && activeCount >= 2) {
            int near = activeCount - 1;
            rows = sitterProfileRepository.findRecommendCandidates(params, near, candidatePool);
            if (!rows.isEmpty()) {
                matchMode = "NEAR_MATCH";
                summary = "필수 조건을 모두 만족하는 시터가 아직 없어, " + near + "가지 이상을 만족하는 시터를 우선 보여드릴게요.";
            }
        }

        // 3차: 거의 일치 후보도 없으면 fallback (불꽃 점수 상위)
        if (rows.isEmpty()) {
            return buildFallbackResponse(limit, "요청하신 조건과 일치하는 시터가 아직 부족해, 평점이 높은 인기 시터를 추천드릴게요.");
        }

        List<SitterRecommendItem> items = new ArrayList<>();
        int rank = 1;
        for (SitterRecommendRow row : rows) {
            int matched = row.matchedCount() != null ? row.matchedCount() : 0;
            int matchScore = (int) Math.round(100.0 * matched / Math.max(activeCount, 1));
            List<String> reasons = buildPerfectMatchReasons(row, params);
            items.add(new SitterRecommendItem(
                    rank++,
                    matchScore,
                    matched,
                    activeCount,
                    reasons,
                    toSearchResponse(row)
            ));
            if (items.size() >= limit) break;
        }

        return new SitterRecommendResponse(
                summary,
                items.size(),
                matchMode,
                items,
                "AI 추천은 참고용이며, 최종 선택은 부모님께 있습니다."
        );
    }

    // ========================
    // 1) 파라미터 정규화
    // ========================

    /** ParentProfile 의 enum/문자열 필드를 정수 구간/문자열로 변환합니다. */
    private RecommendMatchParams buildParams(ParentProfile parent) {
        if (parent == null) {
            return new RecommendMatchParams(null, null, null, null, null, null, null, null, null, 0);
        }

        // 나이 구간
        int[] ageRange = ageRangeFor(parent.getPreferredSitterAgeRange());
        Integer ageLo = ageRange != null ? ageRange[0] : null;
        Integer ageHi = ageRange != null ? ageRange[1] : null;

        // 성별
        String gender = parent.getPreferredSitterGender();

        // 경력 구간
        int[] expRange = experienceRangeFor(parent.getPreferredSitterExperience());
        Integer expLo = expRange != null ? expRange[0] : null;
        Integer expHi = expRange != null ? expRange[1] : null;

        // 국적
        SitterNationalityType nat = parent.getPreferredSitterNationality();
        String nationality = nat != null ? nat.name() : null;

        // 지역
        String sido = parent.getPreferredRegionSido();
        String sigungu = parent.getPreferredRegionSigungu();
        String dong = parent.getPreferredRegionDong();

        int active = 0;
        if (ageLo != null) active++;
        if (gender != null && !gender.isBlank()) active++;
        if (expLo != null || expHi != null) active++;
        if (nationality != null) active++;
        if ((sido != null && !sido.isBlank()) || (sigungu != null && !sigungu.isBlank())) active++;

        return new RecommendMatchParams(
                ageLo, ageHi,
                gender,
                expLo, expHi,
                nationality,
                sido, sigungu, dong,
                active
        );
    }

    private int[] ageRangeFor(PreferredSitterAgeRange v) {
        if (v == null) return null;
        return switch (v) {
            case TWENTIES -> new int[]{20, 29};
            case THIRTIES -> new int[]{30, 39};
            case FORTIES -> new int[]{40, 49};
            case FIFTIES -> new int[]{50, 59};
        };
    }

    private int[] experienceRangeFor(PreferredSitterExperience v) {
        if (v == null) return null;
        return switch (v) {
            case UNDER_1 -> new int[]{0, 0};
            case FROM_2_TO_5 -> new int[]{2, 4};
            case FROM_5_TO_9 -> new int[]{5, 8};
            case OVER_10 -> new int[]{10, Integer.MAX_VALUE};
        };
    }

    // ========================
    // 2) 추천 사유(reasons) 생성
    // ========================

    private List<String> buildPerfectMatchReasons(SitterRecommendRow row, RecommendMatchParams p) {
        List<String> reasons = new ArrayList<>();

        // 매칭된 조건 사유: 매칭된 항목별 한국어 한 줄씩
        if (p.gender() != null && row.gender() != null && row.gender().equalsIgnoreCase(p.gender())) {
            reasons.add("희망 성별(" + genderKo(p.gender()) + ") 조건과 일치해요");
        }
        if (p.ageLo() != null && p.ageHi() != null && row.age() != null
                && row.age() >= p.ageLo() && row.age() <= p.ageHi()) {
            reasons.add("희망 나이대(" + p.ageLo() + "~" + p.ageHi() + "세)에 부합하는 " + row.age() + "세 시터예요");
        }
        if ((p.experienceMinInclusive() != null || p.experienceMaxInclusive() != null) && row.yearsOfExperience() != null) {
            int lo = p.experienceMinInclusive() != null ? p.experienceMinInclusive() : 0;
            int hi = p.experienceMaxInclusive() != null ? p.experienceMaxInclusive() : Integer.MAX_VALUE;
            if (row.yearsOfExperience() >= lo && row.yearsOfExperience() <= hi) {
                reasons.add("희망 경력 구간을 만족하는 경력 " + row.yearsOfExperience() + "년의 시터예요");
            }
        }
        if (p.nationalityType() != null && row.nationalityType() != null
                && row.nationalityType().name().equals(p.nationalityType())) {
            reasons.add("희망 국적(" + nationalityKo(p.nationalityType()) + ") 조건과 일치해요");
        }
        if (regionMatched(row, p)) {
            String label = p.regionSigungu() != null && !p.regionSigungu().isBlank()
                    ? p.regionSigungu() : p.regionSido();
            reasons.add("희망 지역 " + label + " 부근에서 활동하는 시터예요");
        }

        // 보조 사유: 평점/자격증 등 신뢰 신호
        if (row.flameScore() != null && row.flameScore() >= 80) {
            reasons.add("불꽃 점수 " + row.flameScore() + "점(" + safe(row.flameGrade(), "NEW") + ")의 검증된 시터예요");
        }
        if (Boolean.TRUE.equals(row.hasCertificate())) {
            reasons.add("자격증을 보유한 시터예요");
        }

        return reasons;
    }

    private boolean regionMatched(SitterRecommendRow row, RecommendMatchParams p) {
        String r = row.region();
        if (r == null) return false;
        String rLower = r.toLowerCase();
        if (p.regionSigungu() != null && !p.regionSigungu().isBlank()
                && rLower.contains(p.regionSigungu().toLowerCase())) return true;
        if (p.regionSido() != null && !p.regionSido().isBlank()
                && rLower.contains(p.regionSido().toLowerCase())) return true;
        return false;
    }

    // ========================
    // 3) Fallback (차선책)
    // ========================

    private SitterRecommendResponse buildFallbackResponse(int limit, String summary) {
        List<SitterRecommendRow> rows = sitterProfileRepository.findTopByFlameScore(limit);
        List<SitterRecommendItem> items = new ArrayList<>();
        int rank = 1;
        for (SitterRecommendRow row : rows) {
            // 차선책은 매칭 조건이 0개이지만 카드 UX 일관성을 위해 불꽃 점수 기반 추정 매칭률(%)을 채워줍니다.
            int fScore = row.flameScore() != null ? row.flameScore() : 0;
            int matchScore = Math.min(85, 55 + fScore / 4);
            items.add(new SitterRecommendItem(
                    rank++,
                    matchScore,
                    0,
                    0,
                    buildFallbackReasons(row),
                    toSearchResponse(row)
            ));
        }
        return new SitterRecommendResponse(
                summary,
                items.size(),
                "FALLBACK_TOP_SCORE",
                items,
                "AI 추천은 참고용이며, 최종 선택은 부모님께 있습니다."
        );
    }

    private List<String> buildFallbackReasons(SitterRecommendRow row) {
        List<String> reasons = new ArrayList<>();
        reasons.add("현재 인기·평점이 높은 추천 시터예요");
        if (row.flameScore() != null && row.flameScore() > 0) {
            reasons.add("불꽃 점수 " + row.flameScore() + "점(" + safe(row.flameGrade(), "NEW") + ")의 검증된 활동력");
        }
        if (Boolean.TRUE.equals(row.hasCertificate())) {
            reasons.add("자격증을 보유한 시터예요");
        }
        if (row.region() != null && !row.region().isBlank()) {
            reasons.add(row.region() + "에서 활동 중");
        }
        return reasons;
    }

    // ========================
    // 4) 공통 매퍼/유틸
    // ========================

    private SitterSearchResponse toSearchResponse(SitterRecommendRow row) {
        return new SitterSearchResponse(
                row.sitterProfileId(),
                row.userId(),
                row.name(),
                row.bio(),
                row.age(),
                row.gender(),
                row.yearsOfExperience(),
                row.hasCertificate(),
                row.region(),
                row.flameScore() != null ? row.flameScore() : 0,
                safe(row.flameGrade(), "NEW"),
                row.completedReservationCount() != null ? row.completedReservationCount() : 0,
                row.averageRating() != null ? row.averageRating() : 0.0,
                row.recentActivityCount() != null ? row.recentActivityCount() : 0
        );
    }

    private int clampLimit(Integer raw) {
        int v = raw != null ? raw : DEFAULT_LIMIT;
        if (v < MIN_LIMIT) return MIN_LIMIT;
        if (v > MAX_LIMIT) return MAX_LIMIT;
        return v;
    }

    private static String genderKo(String g) {
        if (g == null) return "-";
        if ("FEMALE".equalsIgnoreCase(g)) return "여성";
        if ("MALE".equalsIgnoreCase(g)) return "남성";
        return g;
    }

    private static String nationalityKo(String n) {
        if (n == null) return "-";
        if ("KOREAN".equalsIgnoreCase(n)) return "내국인";
        if ("FOREIGNER".equalsIgnoreCase(n)) return "외국인";
        return n;
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
