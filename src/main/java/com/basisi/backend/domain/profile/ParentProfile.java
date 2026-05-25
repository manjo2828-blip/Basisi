package com.basisi.backend.domain.profile;

// 사용자 엔티티를 참조하기 위한 import입니다.
import com.basisi.backend.domain.user.User;
// JPA 컬럼 어노테이션입니다.
import jakarta.persistence.Column;
// JPA 엔티티 어노테이션입니다.
import jakarta.persistence.Entity;
// 기본 키 생성 어노테이션입니다.
import jakarta.persistence.GeneratedValue;
// 기본 키 생성 전략입니다.
import jakarta.persistence.GenerationType;
// 기본 키 어노테이션입니다.
import jakarta.persistence.Id;
// 조인 컬럼 어노테이션입니다.
import jakarta.persistence.JoinColumn;
// 1:1 연관관계 어노테이션입니다.
import jakarta.persistence.OneToOne;
// 테이블 이름 지정 어노테이션입니다.
import jakarta.persistence.Table;
// 생성/수정 시각 타입입니다.
import java.time.LocalDateTime;
// 생성 직전 콜백 어노테이션입니다.
import jakarta.persistence.PrePersist;
// 수정 직전 콜백 어노테이션입니다.
import jakarta.persistence.PreUpdate;
// 지연 로딩을 위한 fetch 타입입니다.
import jakarta.persistence.FetchType;
// 열거형 저장 방식입니다.
import jakarta.persistence.EnumType;
// 열거형 매핑 어노테이션입니다.
import jakarta.persistence.Enumerated;
// Lombok Getter 어노테이션입니다.
import lombok.Getter;
// Lombok 기본 생성자 어노테이션입니다.
import lombok.NoArgsConstructor;

// 부모 프로필 정보를 저장하는 엔티티입니다.
@Entity
// parent_profiles 테이블과 매핑합니다.
@Table(name = "parent_profiles")
// 필드 조회용 getter를 생성합니다.
@Getter
// JPA 기본 생성자를 생성합니다.
@NoArgsConstructor
public class ParentProfile {

    // 부모 프로필 고유 식별자입니다.
    @Id
    // 자동 증가 전략을 사용합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자와 1:1로 매핑합니다. (한 사용자당 부모 프로필 1개)
    @OneToOne(fetch = FetchType.LAZY)
    // user_id 컬럼으로 users 테이블과 조인합니다. (유니크로 1:1 보장)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 연락 가능한 전화번호를 저장합니다.
    @Column(nullable = false, length = 30)
    private String phoneNumber;

    // 거주 지역(동/구 등 간단 주소)을 저장합니다.
    @Column(nullable = true, length = 100)
    private String region;

    // 아이 정보 요약(나이/특이사항 등)을 간단히 저장합니다.
    @Column(nullable = true, length = 200)
    private String childNote;

    // 시·도
    @Column(name = "region_sido", length = 50)
    private String regionSido;

    // 시·군·구
    @Column(name = "region_sigungu", length = 80)
    private String regionSigungu;

    // 동·읍·면
    @Column(name = "region_dong", length = 80)
    private String regionDong;

    // 맞벌이 / 전업 주부 등
    @Enumerated(EnumType.STRING)
    @Column(name = "parent_work_type", length = 24)
    private ParentWorkType parentWorkType;

    // 맘시터가 필요한 일정 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", length = 24)
    private ParentScheduleType scheduleType;

    // 돌봄 대상으로 선택한 아이 id(children_json 내 id와 일치)
    @Column(name = "care_child_id", length = 64)
    private String careChildId;

    // 등록 아이 목록(JSON 배열)
    @Column(name = "children_json", columnDefinition = "TEXT")
    private String childrenJson = "[]";

    // 맘시터에게 바라는 활동 키워드(JSON 문자열 배열)
    @Column(name = "expectation_keywords_json", columnDefinition = "TEXT")
    private String expectationKeywordsJson = "[]";

    // 맘시터에게 전할 메시지
    @Column(name = "sitter_message", length = 2000)
    private String sitterMessage;

    // 시터 매칭용 - 희망 나이 구간
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_sitter_age_range", length = 24)
    private PreferredSitterAgeRange preferredSitterAgeRange;

    // 시터 매칭용 - 희망 성별 (FEMALE/MALE)
    @Column(name = "preferred_sitter_gender", length = 10)
    private String preferredSitterGender;

    // 시터 매칭용 - 희망 경력 구간
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_sitter_experience", length = 24)
    private PreferredSitterExperience preferredSitterExperience;

    // 시터 매칭용 - 희망 국적 (시터 도메인의 KOREAN/FOREIGNER 와 동일)
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_sitter_nationality", length = 20)
    private SitterNationalityType preferredSitterNationality;

    // 시터 매칭용 - 희망 활동 지역(시·도)
    @Column(name = "preferred_region_sido", length = 50)
    private String preferredRegionSido;

    // 시터 매칭용 - 희망 활동 지역(시·군·구)
    @Column(name = "preferred_region_sigungu", length = 80)
    private String preferredRegionSigungu;

    // 시터 매칭용 - 희망 활동 지역(동·읍·면)
    @Column(name = "preferred_region_dong", length = 80)
    private String preferredRegionDong;

    // 프로필 생성 시각입니다.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 프로필 수정 시각입니다.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성 편의를 위한 생성자입니다.
    public ParentProfile(User user, String phoneNumber, String region, String childNote) {
        // 연관 사용자 정보를 설정합니다.
        this.user = user;
        // 전화번호를 설정합니다.
        this.phoneNumber = phoneNumber;
        // 지역을 설정합니다.
        this.region = region;
        // 아이 노트를 설정합니다.
        this.childNote = childNote;
        this.childrenJson = "[]";
        this.expectationKeywordsJson = "[]";
    }

    /** 저장 요청 전체를 반영합니다(클라이언트가 최신 상태를 한 번에 보냅니다). */
    public void replaceAll(
            String phoneNumber,
            String region,
            String childNote,
            String regionSido,
            String regionSigungu,
            String regionDong,
            ParentWorkType parentWorkType,
            ParentScheduleType scheduleType,
            String careChildId,
            String childrenJson,
            String expectationKeywordsJson,
            String sitterMessage,
            PreferredSitterAgeRange preferredSitterAgeRange,
            String preferredSitterGender,
            PreferredSitterExperience preferredSitterExperience,
            SitterNationalityType preferredSitterNationality,
            String preferredRegionSido,
            String preferredRegionSigungu,
            String preferredRegionDong
    ) {
        this.phoneNumber = phoneNumber;
        this.region = region;
        this.childNote = childNote;
        this.regionSido = regionSido;
        this.regionSigungu = regionSigungu;
        this.regionDong = regionDong;
        this.parentWorkType = parentWorkType;
        this.scheduleType = scheduleType;
        this.careChildId = careChildId;
        this.childrenJson = childrenJson != null ? childrenJson : "[]";
        this.expectationKeywordsJson = expectationKeywordsJson != null ? expectationKeywordsJson : "[]";
        this.sitterMessage = sitterMessage;
        this.preferredSitterAgeRange = preferredSitterAgeRange;
        this.preferredSitterGender = preferredSitterGender;
        this.preferredSitterExperience = preferredSitterExperience;
        this.preferredSitterNationality = preferredSitterNationality;
        this.preferredRegionSido = preferredRegionSido;
        this.preferredRegionSigungu = preferredRegionSigungu;
        this.preferredRegionDong = preferredRegionDong;
    }

    // 최초 저장 시 생성/수정 시각을 자동 설정합니다.
    @PrePersist
    public void prePersist() {
        // 현재 시각을 생성 시각으로 저장합니다.
        this.createdAt = LocalDateTime.now();
        // 현재 시각을 수정 시각으로 저장합니다.
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 시 수정 시각을 자동 갱신합니다.
    @PreUpdate
    public void preUpdate() {
        // 현재 시각으로 수정 시각을 갱신합니다.
        this.updatedAt = LocalDateTime.now();
    }
}

