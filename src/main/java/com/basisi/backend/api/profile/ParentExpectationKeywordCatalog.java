package com.basisi.backend.api.profile;

import java.util.Collections;
import java.util.Set;

/** 맘시터 요청 키워드(화이트리스트)입니다. 프론트 선택지와 동일한 문자열을 유지합니다. */
public final class ParentExpectationKeywordCatalog {

    private ParentExpectationKeywordCatalog() {}

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
            "한글놀이",
            "숙제·학습지 보조",
            "영어놀이",
            "체육놀이",
            "촉감놀이",
            "미술놀이(만들기·그리기)",
            "종이접기",
            "보드게임",
            "음악놀이(피아노·단소 등)",
            "유아글쓰기",
            "줄넘기",
            "자전거 타기",
            "한글·국어 과외",
            "수학·과학 과외",
            "유아 연산수학",
            "유아 영어(리딩)",
            "유아 영어(파닉스)",
            "미술 과외",
            "단체 숲 체험 수업"
    ));

    public static boolean isAllowed(String keyword) {
        return keyword != null && ALLOWED.contains(keyword.trim());
    }
}
