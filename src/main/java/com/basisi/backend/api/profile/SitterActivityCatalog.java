package com.basisi.backend.api.profile;

import java.util.Collections;
import java.util.Set;

/** 시터가 선택할 수 있는 보육·돌봄 활동(화이트리스트)입니다. */
public final class SitterActivityCatalog {

    private SitterActivityCatalog() {}

    public static final Set<String> ALLOWED = Collections.unmodifiableSet(Set.of(
            "구비된 장난감 실내놀이",
            "책 읽어주기",
            "등하원 동행",
            "밥 챙겨주기",
            "집 근처 야외 활동",
            "돌봄 후 뒷정리",
            "재우기/깨우기",
            "아이 위생 관리",
            "샤워/목욕/양치",
            "자차로 등하원 시키기",
            "자차로 야외 활동(예: 문화센터)",
            "산후 관리(산모·신생아 건강관리)"
    ));

    public static boolean isAllowed(String activity) {
        return activity != null && ALLOWED.contains(activity.trim());
    }
}
