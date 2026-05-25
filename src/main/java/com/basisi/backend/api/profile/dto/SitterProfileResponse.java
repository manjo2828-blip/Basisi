package com.basisi.backend.api.profile.dto;

import com.basisi.backend.domain.profile.SitterChildAgePreference;
import com.basisi.backend.domain.profile.SitterNationalityType;

import java.util.List;

/** 시터 프로필 조회 응답 DTO입니다. */
public record SitterProfileResponse(
        Long id,
        Long userId,
        Integer age,
        String gender,
        String phoneNumber,
        Integer yearsOfExperience,
        Boolean hasCertificate,
        String region,
        String bio,
        SitterNationalityType nationalityType,
        List<String> availableActivities,
        Integer childcareHourlyWage,
        Boolean hourlyNegotiable,
        Boolean cctvConsent,
        List<PreferredRegionDto> preferredRegions,
        List<SitterChildAgePreference> preferredAgeGroups,
        List<String> profilePhotoIds
) {
}
