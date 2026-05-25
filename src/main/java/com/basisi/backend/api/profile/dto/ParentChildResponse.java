package com.basisi.backend.api.profile.dto;

import java.time.LocalDate;

/** 부모 프로필에 등록된 아이 정보 응답입니다. */
public record ParentChildResponse(
        String id,
        LocalDate birthDate,
        String gender
) {
}
