package com.basisi.backend.api.profile.dto;

import com.basisi.backend.domain.profile.SitterNationalityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import java.util.List;

/** 시터 프로필 생성/수정 요청 DTO입니다. */
public record SitterProfileUpsertRequest(
        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
        String phoneNumber,
        @NotNull(message = "나이는 필수입니다.")
        @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
        Integer age,
        @NotBlank(message = "성별은 필수입니다.")
        @Size(max = 10, message = "성별 값이 올바르지 않습니다.")
        String gender,
        @NotNull(message = "경력(년)은 필수입니다.")
        @Min(value = 0, message = "경력(년)은 0 이상이어야 합니다.")
        Integer yearsOfExperience,
        @NotNull(message = "자격증 유무는 필수입니다.")
        Boolean hasCertificate,
        @NotBlank(message = "거주 지역은 필수입니다.")
        @Size(max = 60, message = "거주 지역은 60자 이하여야 합니다.")
        String region,
        @Size(max = 2000, message = "소개글은 2000자 이하여야 합니다.")
        String bio,
        SitterNationalityType nationalityType,
        @NotNull(message = "가능한 활동 목록은 null일 수 없습니다.")
        List<@NotBlank @Size(max = 80) String> availableActivities,
        Integer childcareHourlyWage,
        Boolean hourlyNegotiable,
        Boolean cctvConsent,
        @NotNull(message = "희망 지역 목록은 null일 수 없습니다.")
        @Size(max = 3, message = "활동 희망 지역은 최대 3곳까지 선택할 수 있습니다.")
        @Valid
        List<PreferredRegionDto> preferredRegions,
        @NotNull(message = "선호 연령대 목록은 null일 수 없습니다.")
        List<@NotBlank @Size(max = 40) String> preferredAgeGroups,
        @NotNull(message = "프로필 사진 id 목록은 null일 수 없습니다.")
        @Size(max = 5, message = "프로필 사진은 최대 5장까지 등록할 수 있습니다.")
        List<@NotBlank @Size(max = 40) String> profilePhotoIds
) {
}
