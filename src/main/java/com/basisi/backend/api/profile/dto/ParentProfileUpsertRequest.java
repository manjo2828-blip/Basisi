package com.basisi.backend.api.profile.dto;

import com.basisi.backend.domain.profile.ParentScheduleType;
import com.basisi.backend.domain.profile.ParentWorkType;
import com.basisi.backend.domain.profile.PreferredSitterAgeRange;
import com.basisi.backend.domain.profile.PreferredSitterExperience;
import com.basisi.backend.domain.profile.SitterNationalityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 부모 프로필 생성/수정 요청 DTO입니다. */
public record ParentProfileUpsertRequest(
        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
        String phoneNumber,
        @Size(max = 100, message = "지역은 100자 이하여야 합니다.")
        String region,
        @Size(max = 200, message = "아이 정보는 200자 이하여야 합니다.")
        String childNote,
        @Size(max = 50, message = "시·도는 50자 이하여야 합니다.")
        String regionSido,
        @Size(max = 80, message = "시·군·구는 80자 이하여야 합니다.")
        String regionSigungu,
        @Size(max = 80, message = "동·읍·면은 80자 이하여야 합니다.")
        String regionDong,
        ParentWorkType parentWorkType,
        ParentScheduleType scheduleType,
        @Size(max = 64, message = "careChildId는 64자 이하여야 합니다.")
        String careChildId,
        @NotNull(message = "아이 목록은 null일 수 없습니다.")
        @Size(max = 5, message = "아이는 최대 5명까지 등록할 수 있습니다.")
        @Valid
        List<ParentChildDto> children,
        @NotNull(message = "키워드 목록은 null일 수 없습니다.")
        @Size(max = 5, message = "키워드는 최대 5개까지 선택할 수 있습니다.")
        List<@NotBlank @Size(max = 80, message = "키워드 한 항목은 80자 이하여야 합니다.") String> expectationKeywords,
        @Size(max = 2000, message = "메시지는 2000자 이하여야 합니다.")
        String sitterMessage,
        // ===== 시터 매칭용 필수 조건 =====
        PreferredSitterAgeRange preferredSitterAgeRange,
        @Size(max = 10, message = "희망 성별 값이 올바르지 않습니다.")
        String preferredSitterGender,
        PreferredSitterExperience preferredSitterExperience,
        SitterNationalityType preferredSitterNationality,
        @Size(max = 50, message = "희망 지역 시·도는 50자 이하여야 합니다.")
        String preferredRegionSido,
        @Size(max = 80, message = "희망 지역 시·군·구는 80자 이하여야 합니다.")
        String preferredRegionSigungu,
        @Size(max = 80, message = "희망 지역 동·읍·면은 80자 이하여야 합니다.")
        String preferredRegionDong
) {
}
