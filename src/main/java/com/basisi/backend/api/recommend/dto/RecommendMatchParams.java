package com.basisi.backend.api.recommend.dto;

/**
 * QueryDSL CASE WHEN 매칭 점수 계산에 쓰이는 평면 파라미터입니다.
 *
 * 부모 프로필의 5개 enum/문자열 필드를 정수 구간/문자열로 미리 정규화한 뒤 Repository로 넘깁니다.
 *
 * 어떤 필드든 null이면 "부모가 그 조건을 선택하지 않았다"는 뜻이며,
 * Repository는 해당 조건에 대해 항상 0점을 부여합니다(활성화 조건 수에서도 제외).
 */
public record RecommendMatchParams(
        Integer ageLo,
        Integer ageHi,
        String gender,
        Integer experienceMinInclusive,
        Integer experienceMaxInclusive,
        String nationalityType,
        String regionSido,
        String regionSigungu,
        String regionDong,
        Integer activeConditionCount
) {
}
