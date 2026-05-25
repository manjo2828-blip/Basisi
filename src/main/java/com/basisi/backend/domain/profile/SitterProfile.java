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
import java.time.LocalTime;
// 생성 직전 콜백 어노테이션입니다.
import jakarta.persistence.PrePersist;
// 수정 직전 콜백 어노테이션입니다.
import jakarta.persistence.PreUpdate;
// 지연 로딩을 위한 fetch 타입입니다.
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
// Lombok Getter 어노테이션입니다.
import lombok.Getter;
// Lombok 기본 생성자 어노테이션입니다.
import lombok.NoArgsConstructor;

// 시터 프로필 정보를 저장하는 엔티티입니다.
@Entity
// sitter_profiles 테이블과 매핑합니다.
@Table(name = "sitter_profiles")
// 필드 조회용 getter를 생성합니다.
@Getter
// JPA 기본 생성자를 생성합니다.
@NoArgsConstructor
public class SitterProfile {

    // 시터 프로필 고유 식별자입니다.
    @Id
    // 자동 증가 전략을 사용합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자와 1:1로 매핑합니다. (한 사용자당 시터 프로필 1개)
    @OneToOne(fetch = FetchType.LAZY)
    // user_id 컬럼으로 users 테이블과 조인합니다. (유니크로 1:1 보장)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 연락 가능한 전화번호를 저장합니다.
    @Column(nullable = false, length = 30)
    private String phoneNumber;

    // 시터 소개글을 저장합니다.
    @Column(nullable = true, length = 2000)
    private String bio;

    // 나이를 저장합니다.
    @Column(nullable = true)
    private Integer age;

    // 성별을 저장합니다. (FEMALE/MALE)
    @Column(nullable = true, length = 10)
    private String gender;

    // 시터 경력(년)을 저장합니다.
    @Column(nullable = true)
    private Integer yearsOfExperience;

    // 자격증 보유 여부를 저장합니다.
    @Column(nullable = true)
    private Boolean hasCertificate;

    // 거주 지역을 저장합니다.
    @Column(nullable = true, length = 60)
    private String region;

    // 내국인/외국인
    @Enumerated(EnumType.STRING)
    @Column(name = "nationality_type", length = 20)
    private SitterNationalityType nationalityType;

    // 가능한 활동(보육·돌봄 키워드 JSON 배열)
    @Column(name = "available_activities_json", columnDefinition = "TEXT")
    private String availableActivitiesJson = "[]";

    // 보육·돌봄 희망 시급(원)
    @Column(name = "childcare_hourly_wage")
    private Integer childcareHourlyWage;

    // 시급 협의 가능 여부
    @Column(name = "hourly_negotiable")
    private Boolean hourlyNegotiable;

    // CCTV 촬영 동의(null: 미선택)
    @Column(name = "cctv_consent")
    private Boolean cctvConsent;

    // 활동 희망 지역(JSON, 최대 3곳)
    @Column(name = "preferred_regions_json", columnDefinition = "TEXT")
    private String preferredRegionsJson = "[]";

    // 선호 아이 연령대(JSON enum 이름 배열)
    @Column(name = "preferred_age_groups_json", columnDefinition = "TEXT")
    private String preferredAgeGroupsJson = "[]";

    // 프로필 사진 id 목록(JSON, sitter_profile_images.id)
    @Column(name = "profile_photo_ids_json", columnDefinition = "TEXT")
    private String profilePhotoIdsJson = "[]";

    // ---- (legacy) 기존 위치/시급 기반 탐색 필드: 화면에서는 사용하지 않지만 데이터 호환을 위해 유지합니다. ----
    // 시급(원)을 저장합니다.
    @Column(nullable = true)
    private Integer hourlyWage;

    // 지도 기반 검색을 위해 위도를 저장합니다.
    @Column(nullable = true)
    private Double latitude;

    // 지도 기반 검색을 위해 경도를 저장합니다.
    @Column(nullable = true)
    private Double longitude;

    // 기본 가능 시작 시간을 저장합니다.
    @Column(nullable = true)
    private LocalTime availableStartTime;

    // 기본 가능 종료 시간을 저장합니다.
    @Column(nullable = true)
    private LocalTime availableEndTime;

    // 프로필 생성 시각입니다.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 프로필 수정 시각입니다.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성 편의를 위한 생성자입니다.
    public SitterProfile(
            User user,
            String phoneNumber,
            Integer age,
            String gender,
            Integer yearsOfExperience,
            Boolean hasCertificate,
            String region,
            String bio,
            Integer hourlyWage,
            Double latitude,
            Double longitude,
            LocalTime availableStartTime,
            LocalTime availableEndTime
    ) {
        // 연관 사용자 정보를 설정합니다.
        this.user = user;
        // 전화번호를 설정합니다.
        this.phoneNumber = phoneNumber;
        // 나이를 설정합니다.
        this.age = age;
        // 성별을 설정합니다.
        this.gender = gender;
        // 경력(년)을 설정합니다.
        this.yearsOfExperience = yearsOfExperience;
        // 자격증 유무를 설정합니다.
        this.hasCertificate = hasCertificate;
        // 거주 지역을 설정합니다.
        this.region = region;
        // 소개글을 설정합니다.
        this.bio = bio;
        this.availableActivitiesJson = "[]";
        this.preferredRegionsJson = "[]";
        this.preferredAgeGroupsJson = "[]";
        this.profilePhotoIdsJson = "[]";
        // 시급을 설정합니다.
        this.hourlyWage = hourlyWage;
        // 위도를 설정합니다.
        this.latitude = latitude;
        // 경도를 설정합니다.
        this.longitude = longitude;
        // 가능 시작 시간을 설정합니다.
        this.availableStartTime = availableStartTime;
        // 가능 종료 시간을 설정합니다.
        this.availableEndTime = availableEndTime;
    }

    /** 클라이언트가 보낸 최신 프로필 전체를 반영합니다. */
    public void replaceAll(
            String phoneNumber,
            Integer age,
            String gender,
            Integer yearsOfExperience,
            Boolean hasCertificate,
            String region,
            String bio,
            SitterNationalityType nationalityType,
            String availableActivitiesJson,
            Integer childcareHourlyWage,
            Boolean hourlyNegotiable,
            Boolean cctvConsent,
            String preferredRegionsJson,
            String preferredAgeGroupsJson,
            String profilePhotoIdsJson,
            Integer legacyHourlyWage,
            Double latitude,
            Double longitude,
            LocalTime availableStartTime,
            LocalTime availableEndTime
    ) {
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.gender = gender;
        this.yearsOfExperience = yearsOfExperience;
        this.hasCertificate = hasCertificate;
        this.region = region;
        this.bio = bio;
        this.nationalityType = nationalityType;
        this.availableActivitiesJson = availableActivitiesJson != null ? availableActivitiesJson : "[]";
        this.childcareHourlyWage = childcareHourlyWage;
        this.hourlyNegotiable = hourlyNegotiable;
        this.cctvConsent = cctvConsent;
        this.preferredRegionsJson = preferredRegionsJson != null ? preferredRegionsJson : "[]";
        this.preferredAgeGroupsJson = preferredAgeGroupsJson != null ? preferredAgeGroupsJson : "[]";
        this.profilePhotoIdsJson = profilePhotoIdsJson != null ? profilePhotoIdsJson : "[]";
        this.hourlyWage = legacyHourlyWage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availableStartTime = availableStartTime;
        this.availableEndTime = availableEndTime;
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

