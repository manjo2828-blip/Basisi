package com.basisi.backend.api.profile.dto;

import com.basisi.backend.domain.profile.ParentScheduleType;
import com.basisi.backend.domain.profile.ParentWorkType;
import com.basisi.backend.domain.profile.PreferredSitterAgeRange;
import com.basisi.backend.domain.profile.PreferredSitterExperience;
import com.basisi.backend.domain.profile.SitterNationalityType;

import java.util.List;

/** 부모 프로필 조회 응답 DTO입니다. */
public record ParentProfileResponse(
        Long id,
        Long userId,
        String phoneNumber,
        String region,
        String childNote,
        String regionSido,
        String regionSigungu,
        String regionDong,
        ParentWorkType parentWorkType,
        ParentScheduleType scheduleType,
        String careChildId,
        List<ParentChildResponse> children,
        List<String> expectationKeywords,
        String sitterMessage,
        PreferredSitterAgeRange preferredSitterAgeRange,
        String preferredSitterGender,
        PreferredSitterExperience preferredSitterExperience,
        SitterNationalityType preferredSitterNationality,
        String preferredRegionSido,
        String preferredRegionSigungu,
        String preferredRegionDong
) {
}
