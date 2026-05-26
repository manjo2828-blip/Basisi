package com.basisi.backend.ai;

import com.basisi.backend.api.recommend.dto.RecommendMatchParams;
import com.basisi.backend.api.recommend.dto.SitterRecommendItem;
import com.basisi.backend.domain.profile.ParentProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 부모 조건 + 후보 시터 JSON을 LLM 프롬프트로 변환합니다. */
@Component
public class RecommendPromptBuilder {

    private final ObjectMapper objectMapper;

    public RecommendPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildSystemInstruction() {
        return """
                당신은 프리미엄 베이비시터 매칭 플랫폼 '베시시(Basisi)'의 AI 추천 어시스턴트입니다.
                입력으로 주어진 후보 시터 목록만 사용해 재순위하고, 부모에게 친근한 한국어 추천 사유를 작성하세요.

                규칙:
                1) candidates 배열에 없는 sitterProfileId는 절대 출력하지 마세요.
                2) 각 후보당 reasons는 1~2개, 각 1~2줄의 자연스러운 한국어로 작성하세요.
                3) summary는 부모 조건과 추가 요청을 반영한 1~2문장 한국어 요약입니다.
                4) rank는 1부터 시작하며 중복 없이 매깁니다.
                5) 후보 수를 초과하는 item을 만들지 마세요.
                6) JSON만 반환하세요. 마크다운이나 설명 문장은 금지합니다.
                """;
    }

    public String buildUserPrompt(
            ParentProfile parent,
            RecommendMatchParams params,
            String additionalRequest,
            List<SitterRecommendItem> candidates,
            int limit
    ) throws JsonProcessingException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("limit", limit);
        payload.put("parentContext", buildParentContext(parent, params, additionalRequest));
        payload.put("candidates", candidates.stream().map(this::toCandidateMap).collect(Collectors.toList()));
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    }

    private Map<String, Object> buildParentContext(
            ParentProfile parent,
            RecommendMatchParams params,
            String additionalRequest
    ) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        if (parent != null) {
            ctx.put("region", parent.getRegion());
            ctx.put("regionSido", parent.getRegionSido());
            ctx.put("regionSigungu", parent.getRegionSigungu());
            ctx.put("regionDong", parent.getRegionDong());
            ctx.put("childrenJson", parent.getChildrenJson());
            ctx.put("expectationKeywordsJson", parent.getExpectationKeywordsJson());
            ctx.put("sitterMessage", parent.getSitterMessage());
            ctx.put("parentWorkType", parent.getParentWorkType() != null ? parent.getParentWorkType().name() : null);
            ctx.put("scheduleType", parent.getScheduleType() != null ? parent.getScheduleType().name() : null);
        }
        ctx.put("preferredGender", params.gender());
        ctx.put("preferredAgeRange", params.ageLo() != null && params.ageHi() != null
                ? params.ageLo() + "~" + params.ageHi()
                : null);
        ctx.put("preferredExperienceYears", params.experienceMinInclusive() != null || params.experienceMaxInclusive() != null
                ? (params.experienceMinInclusive() != null ? params.experienceMinInclusive() : 0)
                + "~"
                + (params.experienceMaxInclusive() != null ? params.experienceMaxInclusive() : "∞")
                : null);
        ctx.put("preferredNationality", params.nationalityType());
        ctx.put("preferredRegionSido", params.regionSido());
        ctx.put("preferredRegionSigungu", params.regionSigungu());
        ctx.put("additionalRequest", additionalRequest != null && !additionalRequest.isBlank() ? additionalRequest : null);
        return ctx;
    }

    private Map<String, Object> toCandidateMap(SitterRecommendItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        var sitter = item.sitter();
        map.put("sitterProfileId", sitter.sitterProfileId());
        map.put("name", sitter.name());
        map.put("bio", sitter.bio());
        map.put("age", sitter.age());
        map.put("gender", sitter.gender());
        map.put("yearsOfExperience", sitter.yearsOfExperience());
        map.put("hasCertificate", sitter.hasCertificate());
        map.put("region", sitter.region());
        map.put("flameScore", sitter.flameScore());
        map.put("flameGrade", sitter.flameGrade());
        map.put("averageRating", sitter.averageRating());
        map.put("completedReservationCount", sitter.completedReservationCount());
        map.put("algorithmRank", item.rank());
        map.put("matchScore", item.matchScore());
        map.put("matchedConditions", item.matchedConditions());
        map.put("totalConditions", item.totalConditions());
        map.put("algorithmReasons", item.reasons());
        return map;
    }
}
