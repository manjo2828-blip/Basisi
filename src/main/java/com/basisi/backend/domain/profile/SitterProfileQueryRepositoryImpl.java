package com.basisi.backend.domain.profile;

import com.basisi.backend.api.recommend.dto.RecommendMatchParams;
import com.basisi.backend.api.recommend.dto.SitterRecommendRow;
import com.basisi.backend.api.sitter.dto.SitterSearchResponse;
import com.basisi.backend.domain.score.SitterScore;
import com.basisi.backend.domain.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

// QueryDSL 기반 시터 탐색 구현체입니다.
@Repository
public class SitterProfileQueryRepositoryImpl implements SitterProfileQueryRepository {

    // QueryDSL JPAQueryFactory입니다.
    private final JPAQueryFactory queryFactory;

    // 생성자로 QueryFactory를 주입받습니다.
    public SitterProfileQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        // 주입받은 QueryFactory를 필드에 저장합니다.
        this.queryFactory = queryFactory;
    }

    // 시터 검색 + 페이징 + 정렬을 수행합니다.
    @Override
    public Page<SitterSearchResponse> searchSitters(
            String region,
            String gender,
            Integer minYearsOfExperience,
            Pageable pageable
    ) {
        // 시터 프로필 엔티티 경로를 생성합니다.
        PathBuilder<SitterProfile> sitter = new PathBuilder<>(SitterProfile.class, "sitterProfile");
        // 사용자 엔티티 경로를 생성합니다.
        PathBuilder<User> user = new PathBuilder<>(User.class, "user");
        // 시터 점수 엔티티 경로를 생성합니다.
        PathBuilder<SitterScore> score = new PathBuilder<>(SitterScore.class, "sitterScore");

        // 동적 where 조건을 구성합니다.
        BooleanBuilder where = new BooleanBuilder();
        // 지역 조건을 적용합니다. (부분 일치)
        if (region != null && !region.isBlank()) {
            where.and(sitter.getString("region").containsIgnoreCase(region.trim()));
        }
        // 성별 조건을 적용합니다.
        if (gender != null && !gender.isBlank()) {
            where.and(sitter.getString("gender").equalsIgnoreCase(gender.trim()));
        }
        // 최소 경력 조건을 적용합니다.
        if (minYearsOfExperience != null) {
            where.and(sitter.getNumber("yearsOfExperience", Integer.class).goe(minYearsOfExperience));
        }

        // 실제 데이터 조회 쿼리를 구성합니다.
        JPAQuery<SitterSearchResponse> contentQuery = queryFactory
                .select(Projections.constructor(
                        SitterSearchResponse.class,
                        sitter.getNumber("id", Long.class),
                        user.getNumber("id", Long.class),
                        user.getString("name"),
                        sitter.getString("bio"),
                        sitter.getNumber("age", Integer.class),
                        sitter.getString("gender"),
                        sitter.getNumber("yearsOfExperience", Integer.class),
                        sitter.getBoolean("hasCertificate"),
                        sitter.getString("region"),
                        score.getNumber("score", Integer.class).coalesce(0),
                        score.getString("grade").coalesce("NEW"),
                        score.getNumber("completedReservationCount", Integer.class).coalesce(0),
                        score.getNumber("averageRating", Double.class).coalesce(0.0),
                        score.getNumber("recentActivityCount", Integer.class).coalesce(0)
                ))
                .from(sitter)
                .join(sitter.get("user", User.class), user)
                .leftJoin(score).on(score.getNumber("sitterProfileId", Long.class).eq(sitter.getNumber("id", Long.class)))
                .where(where);

        // count 쿼리를 구성합니다.
        JPAQuery<Long> countQuery = queryFactory
                .select(sitter.count())
                .from(sitter)
                .where(where);

        // 정렬 조건을 동적으로 적용합니다.
        applySort(contentQuery, pageable.getSort(), sitter);
        // 페이지 오프셋을 적용합니다.
        contentQuery.offset(pageable.getOffset());
        // 페이지 크기를 적용합니다.
        contentQuery.limit(pageable.getPageSize());

        // 결과 목록을 조회합니다.
        List<SitterSearchResponse> content = contentQuery.fetch();
        // 전체 건수를 조회합니다.
        Long total = countQuery.fetchOne();

        // Page 객체로 감싸서 반환합니다.
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 부모 5조건 매칭 점수(CASE WHEN 합산)를 DB에서 직접 계산하고,
     * matchedCount >= minMatched 인 후보만 ORDER BY matchedCount DESC, flameScore DESC 로 정렬해 LIMIT 만큼 반환합니다.
     * 풀스캔을 막기 위해 LIMIT 을 반드시 사용하며, WHERE 절은 BooleanBuilder 로 동적 구성됩니다.
     */
    @Override
    public List<SitterRecommendRow> findRecommendCandidates(RecommendMatchParams params, int minMatched, int limit) {
        // 엔티티 경로를 구성합니다.
        PathBuilder<SitterProfile> sitter = new PathBuilder<>(SitterProfile.class, "sitterProfile");
        PathBuilder<User> user = new PathBuilder<>(User.class, "user");
        PathBuilder<SitterScore> score = new PathBuilder<>(SitterScore.class, "sitterScore");

        // 각 조건의 1점(CASE WHEN ... THEN 1 ELSE 0)을 합산합니다.
        NumberExpression<Integer> matched = Expressions.asNumber(0);

        // 1) 성별 매칭
        if (params.gender() != null && !params.gender().isBlank()) {
            BooleanExpression cond = sitter.getString("gender").equalsIgnoreCase(params.gender());
            matched = matched.add(new CaseBuilder().when(cond).then(1).otherwise(0));
        }

        // 2) 나이 매칭 (구간)
        if (params.ageLo() != null && params.ageHi() != null) {
            BooleanExpression cond = sitter.getNumber("age", Integer.class).between(params.ageLo(), params.ageHi());
            matched = matched.add(new CaseBuilder().when(cond).then(1).otherwise(0));
        }

        // 3) 경력 매칭 (구간)
        if (params.experienceMinInclusive() != null || params.experienceMaxInclusive() != null) {
            int lo = params.experienceMinInclusive() != null ? params.experienceMinInclusive() : 0;
            int hi = params.experienceMaxInclusive() != null ? params.experienceMaxInclusive() : Integer.MAX_VALUE;
            BooleanExpression cond = sitter.getNumber("yearsOfExperience", Integer.class).between(lo, hi);
            matched = matched.add(new CaseBuilder().when(cond).then(1).otherwise(0));
        }

        // 4) 국적 매칭 (enum String 컬럼 비교)
        if (params.nationalityType() != null && !params.nationalityType().isBlank()) {
            SitterNationalityType targetEnum = SitterNationalityType.valueOf(params.nationalityType());
            BooleanExpression cond = sitter.get("nationalityType", SitterNationalityType.class).eq(targetEnum);
            matched = matched.add(new CaseBuilder().when(cond).then(1).otherwise(0));
        }

        // 5) 지역 매칭 (시·군·구 우선, 그다음 시·도 부분 일치)
        BooleanExpression regionCond = buildRegionMatchCondition(sitter, params);
        if (regionCond != null) {
            matched = matched.add(new CaseBuilder().when(regionCond).then(1).otherwise(0));
        }

        // 호출자가 요구한 최소 매칭 임계값을 적용합니다(0이면 사실상 전체).
        int threshold = Math.max(minMatched, 0);
        BooleanBuilder where = new BooleanBuilder();
        where.and(matched.goe(threshold));
        // threshold 가 0 이면 조건 미설정 시 모두 통과하므로 fallback 와 구분되도록 최소 1 이상으로만 한정합니다.
        if (threshold <= 0) {
            where = new BooleanBuilder();
            where.and(matched.gt(0));
        }

        // 최종 쿼리 구성: DB가 정렬·LIMIT 까지 처리해서 메모리 부담을 최소화합니다.
        return queryFactory
                .select(Projections.constructor(
                        SitterRecommendRow.class,
                        sitter.getNumber("id", Long.class),
                        user.getNumber("id", Long.class),
                        user.getString("name"),
                        sitter.getString("bio"),
                        sitter.getNumber("age", Integer.class),
                        sitter.getString("gender"),
                        sitter.getNumber("yearsOfExperience", Integer.class),
                        sitter.getBoolean("hasCertificate"),
                        sitter.getString("region"),
                        sitter.get("nationalityType", SitterNationalityType.class),
                        score.getNumber("score", Integer.class).coalesce(0),
                        score.getString("grade").coalesce("NEW"),
                        score.getNumber("completedReservationCount", Integer.class).coalesce(0),
                        score.getNumber("averageRating", Double.class).coalesce(0.0),
                        score.getNumber("recentActivityCount", Integer.class).coalesce(0),
                        matched
                ))
                .from(sitter)
                .join(sitter.get("user", User.class), user)
                .leftJoin(score).on(score.getNumber("sitterProfileId", Long.class).eq(sitter.getNumber("id", Long.class)))
                .where(where)
                .orderBy(matched.desc(), score.getNumber("score", Integer.class).coalesce(0).desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 차선책(Fallback) 쿼리: 매칭 후보가 없을 때 불꽃 점수 순으로 상위 limit 명을 반환합니다.
     * matchedCount 컬럼은 항상 0으로 채워 Service 가 fallback 경로임을 식별할 수 있게 합니다.
     */
    @Override
    public List<SitterRecommendRow> findTopByFlameScore(int limit) {
        PathBuilder<SitterProfile> sitter = new PathBuilder<>(SitterProfile.class, "sitterProfile");
        PathBuilder<User> user = new PathBuilder<>(User.class, "user");
        PathBuilder<SitterScore> score = new PathBuilder<>(SitterScore.class, "sitterScore");

        return queryFactory
                .select(Projections.constructor(
                        SitterRecommendRow.class,
                        sitter.getNumber("id", Long.class),
                        user.getNumber("id", Long.class),
                        user.getString("name"),
                        sitter.getString("bio"),
                        sitter.getNumber("age", Integer.class),
                        sitter.getString("gender"),
                        sitter.getNumber("yearsOfExperience", Integer.class),
                        sitter.getBoolean("hasCertificate"),
                        sitter.getString("region"),
                        sitter.get("nationalityType", SitterNationalityType.class),
                        score.getNumber("score", Integer.class).coalesce(0),
                        score.getString("grade").coalesce("NEW"),
                        score.getNumber("completedReservationCount", Integer.class).coalesce(0),
                        score.getNumber("averageRating", Double.class).coalesce(0.0),
                        score.getNumber("recentActivityCount", Integer.class).coalesce(0),
                        Expressions.asNumber(0)
                ))
                .from(sitter)
                .join(sitter.get("user", User.class), user)
                .leftJoin(score).on(score.getNumber("sitterProfileId", Long.class).eq(sitter.getNumber("id", Long.class)))
                .orderBy(score.getNumber("score", Integer.class).coalesce(0).desc(),
                        sitter.getNumber("id", Long.class).desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 부모가 입력한 시·도/시·군·구 정보를 시터 region 필드와 부분 일치로 매칭합니다.
     * 시·군·구가 있으면 그것을 우선, 없으면 시·도만이라도 부분 일치를 시도합니다.
     * 둘 다 비어 있으면 null 을 돌려 매칭 합산에서 제외합니다.
     */
    private BooleanExpression buildRegionMatchCondition(PathBuilder<SitterProfile> sitter, RecommendMatchParams params) {
        boolean hasSigungu = params.regionSigungu() != null && !params.regionSigungu().isBlank();
        boolean hasSido = params.regionSido() != null && !params.regionSido().isBlank();
        if (!hasSigungu && !hasSido) {
            return null;
        }
        BooleanExpression cond = null;
        if (hasSigungu) {
            cond = sitter.getString("region").containsIgnoreCase(params.regionSigungu().trim());
        }
        if (hasSido) {
            BooleanExpression sidoCond = sitter.getString("region").containsIgnoreCase(params.regionSido().trim());
            cond = (cond == null) ? sidoCond : cond.or(sidoCond);
        }
        return cond;
    }

    // 정렬 조건을 쿼리에 적용합니다.
    private void applySort(
            JPAQuery<SitterSearchResponse> query,
            Sort sort,
            PathBuilder<SitterProfile> sitter
    ) {
        // 지정된 정렬이 없으면 기본 정렬을 적용합니다.
        if (sort.isUnsorted()) {
            // 기본값은 최신 프로필(id 내림차순)입니다.
            query.orderBy(sitter.getNumber("id", Long.class).desc());
            return;
        }

        // 다중 정렬을 순서대로 변환합니다.
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        for (Sort.Order order : sort) {
            // 정렬 방향을 변환합니다.
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            // 허용된 엔티티 필드 정렬을 처리합니다.
            if ("yearsOfExperience".equals(order.getProperty())) {
                orderSpecifiers.add(new OrderSpecifier<>(direction, sitter.getNumber("yearsOfExperience", Integer.class)));
                continue;
            }
            if ("createdAt".equals(order.getProperty())) {
                orderSpecifiers.add(new OrderSpecifier<>(direction, sitter.getDateTime("createdAt", java.time.LocalDateTime.class)));
                continue;
            }
            if ("flameScore".equals(order.getProperty())) {
                PathBuilder<SitterScore> score = new PathBuilder<>(SitterScore.class, "sitterScore");
                orderSpecifiers.add(new OrderSpecifier<>(direction, score.getNumber("score", Integer.class).coalesce(0)));
                continue;
            }
            if ("age".equals(order.getProperty())) {
                orderSpecifiers.add(new OrderSpecifier<>(direction, sitter.getNumber("age", Integer.class)));
                continue;
            }
            if ("id".equals(order.getProperty())) {
                orderSpecifiers.add(new OrderSpecifier<>(direction, sitter.getNumber("id", Long.class)));
            }
        }

        // 변환된 정렬이 있으면 적용합니다.
        if (!orderSpecifiers.isEmpty()) {
            query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
        }
    }
}
