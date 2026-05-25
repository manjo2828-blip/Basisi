package com.basisi.backend.service;

import com.basisi.backend.api.profile.ParentExpectationKeywordCatalog;
import com.basisi.backend.api.profile.SitterActivityCatalog;
import com.basisi.backend.api.profile.dto.ParentChildDto;
import com.basisi.backend.api.profile.dto.ParentChildResponse;
import com.basisi.backend.api.profile.dto.ParentProfileResponse;
import com.basisi.backend.api.profile.dto.ParentProfileUpsertRequest;
import com.basisi.backend.api.profile.dto.PreferredRegionDto;
import com.basisi.backend.api.profile.dto.SitterImageUploadResponse;
import com.basisi.backend.api.profile.dto.SitterProfileResponse;
import com.basisi.backend.api.profile.dto.SitterProfileUpsertRequest;
import com.basisi.backend.domain.profile.ParentProfile;
import com.basisi.backend.domain.profile.ParentProfileRepository;
import com.basisi.backend.domain.profile.SitterChildAgePreference;
import com.basisi.backend.domain.profile.SitterProfile;
import com.basisi.backend.domain.profile.SitterProfileImage;
import com.basisi.backend.domain.profile.SitterProfileImageRepository;
import com.basisi.backend.domain.profile.SitterProfileRepository;
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.domain.user.UserRole;
import com.basisi.backend.security.SecurityUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final SitterProfileRepository sitterProfileRepository;
    private final SitterProfileImageRepository sitterProfileImageRepository;
    private final ObjectMapper objectMapper;

    public ProfileService(
            UserRepository userRepository,
            ParentProfileRepository parentProfileRepository,
            SitterProfileRepository sitterProfileRepository,
            SitterProfileImageRepository sitterProfileImageRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.sitterProfileRepository = sitterProfileRepository;
        this.sitterProfileImageRepository = sitterProfileImageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ParentProfileResponse getMyParentProfile() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.PARENT) {
            throw new IllegalArgumentException("부모 계정만 부모 프로필을 조회할 수 있습니다.");
        }
        ParentProfile profile = parentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("부모 프로필이 존재하지 않습니다."));
        return toParentProfileResponse(profile);
    }

    @Transactional
    public ParentProfileResponse upsertMyParentProfile(ParentProfileUpsertRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.PARENT) {
            throw new IllegalArgumentException("부모 계정만 부모 프로필을 저장할 수 있습니다.");
        }

        validateParentProfileRequest(request);

        String childrenJson = writeJson(request.children());
        String keywordsJson = writeJson(request.expectationKeywords());
        String derivedRegion = deriveRegionLabel(request);

        ParentProfile profile = parentProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profile = new ParentProfile(user, request.phoneNumber(), derivedRegion, request.childNote());
        }
        profile.replaceAll(
                request.phoneNumber(),
                derivedRegion,
                request.childNote(),
                trimToNull(request.regionSido()),
                trimToNull(request.regionSigungu()),
                trimToNull(request.regionDong()),
                request.parentWorkType(),
                request.scheduleType(),
                normalizeCareChildId(request),
                childrenJson,
                keywordsJson,
                request.sitterMessage(),
                request.preferredSitterAgeRange(),
                normalizePreferredGender(request.preferredSitterGender()),
                request.preferredSitterExperience(),
                request.preferredSitterNationality(),
                trimToNull(request.preferredRegionSido()),
                trimToNull(request.preferredRegionSigungu()),
                trimToNull(request.preferredRegionDong())
        );
        ParentProfile saved = parentProfileRepository.save(profile);
        return toParentProfileResponse(saved);
    }

    private static void validateParentProfileRequest(ParentProfileUpsertRequest request) {
        List<ParentChildDto> children = request.children();
        Set<String> ids = new HashSet<>();
        for (ParentChildDto c : children) {
            if (!ids.add(c.id())) {
                throw new IllegalArgumentException("아이 식별자가 중복되었습니다.");
            }
        }
        for (String keyword : request.expectationKeywords()) {
            if (!ParentExpectationKeywordCatalog.isAllowed(keyword)) {
                throw new IllegalArgumentException("허용되지 않은 키워드입니다: " + keyword);
            }
        }
        if (request.expectationKeywords().size() != new LinkedHashSet<>(request.expectationKeywords()).size()) {
            throw new IllegalArgumentException("키워드가 중복되었습니다.");
        }

        String careId = trimToNull(request.careChildId());
        if (careId != null) {
            if (children.isEmpty()) {
                throw new IllegalArgumentException("아이 정보가 없으면 돌봄 대상을 선택할 수 없습니다.");
            }
            boolean found = children.stream().anyMatch(c -> c.id().equals(careId));
            if (!found) {
                throw new IllegalArgumentException("돌봄 대상 아이 id가 등록된 아이 목록에 없습니다.");
            }
        }
    }

    private static String normalizePreferredGender(String raw) {
        String v = trimToNull(raw);
        if (v == null) {
            return null;
        }
        String upper = v.toUpperCase();
        if (!"MALE".equals(upper) && !"FEMALE".equals(upper)) {
            throw new IllegalArgumentException("희망 성별 값은 MALE 또는 FEMALE 이어야 합니다.");
        }
        return upper;
    }

    private static String normalizeCareChildId(ParentProfileUpsertRequest request) {
        if (request.children().isEmpty()) {
            return null;
        }
        return trimToNull(request.careChildId());
    }

    private static String deriveRegionLabel(ParentProfileUpsertRequest request) {
        String sido = trimToNull(request.regionSido());
        String sigungu = trimToNull(request.regionSigungu());
        String dong = trimToNull(request.regionDong());
        if (sido != null && sigungu != null && dong != null) {
            return sido + " " + sigungu + " " + dong;
        }
        return request.region();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("프로필 데이터 직렬화에 실패했습니다.", e);
        }
    }

    private ParentProfileResponse toParentProfileResponse(ParentProfile profile) {
        return new ParentProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getPhoneNumber(),
                profile.getRegion(),
                profile.getChildNote(),
                profile.getRegionSido(),
                profile.getRegionSigungu(),
                profile.getRegionDong(),
                profile.getParentWorkType(),
                profile.getScheduleType(),
                profile.getCareChildId(),
                readChildren(profile.getChildrenJson()),
                readKeywords(profile.getExpectationKeywordsJson()),
                profile.getSitterMessage(),
                profile.getPreferredSitterAgeRange(),
                profile.getPreferredSitterGender(),
                profile.getPreferredSitterExperience(),
                profile.getPreferredSitterNationality(),
                profile.getPreferredRegionSido(),
                profile.getPreferredRegionSigungu(),
                profile.getPreferredRegionDong()
        );
    }

    private List<ParentChildResponse> readChildren(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<ParentChildDto> list = objectMapper.readValue(json, new TypeReference<>() {
            });
            return list.stream()
                    .map(c -> new ParentChildResponse(c.id(), c.birthDate(), c.gender()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> readKeywords(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public SitterProfileResponse getMySitterProfile() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.SITTER) {
            throw new IllegalArgumentException("시터 계정만 시터 프로필을 조회할 수 있습니다.");
        }
        SitterProfile profile = sitterProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필이 존재하지 않습니다."));
        return toSitterProfileResponse(profile);
    }

    @Transactional
    public SitterProfileResponse upsertMySitterProfile(SitterProfileUpsertRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.SITTER) {
            throw new IllegalArgumentException("시터 계정만 시터 프로필을 저장할 수 있습니다.");
        }

        validateSitterProfileRequest(request, user.getId());

        List<String> oldPhotoIds = readStringList(
                sitterProfileRepository.findByUserId(user.getId()).map(SitterProfile::getProfilePhotoIdsJson).orElse("[]")
        );

        String activitiesJson = writeJson(request.availableActivities());
        String regionsJson = writeJson(request.preferredRegions());
        List<SitterChildAgePreference> ageEnums = parseAgeGroups(request.preferredAgeGroups());
        String agesJson = writeJson(ageEnums.stream().map(Enum::name).toList());
        String photoIdsJson = writeJson(request.profilePhotoIds());

        SitterProfile profile = sitterProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            profile = new SitterProfile(
                    user,
                    request.phoneNumber(),
                    request.age(),
                    request.gender(),
                    request.yearsOfExperience(),
                    request.hasCertificate(),
                    request.region(),
                    request.bio(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        profile.replaceAll(
                request.phoneNumber(),
                request.age(),
                request.gender(),
                request.yearsOfExperience(),
                request.hasCertificate(),
                request.region(),
                request.bio(),
                request.nationalityType(),
                activitiesJson,
                request.childcareHourlyWage(),
                request.hourlyNegotiable(),
                request.cctvConsent(),
                regionsJson,
                agesJson,
                photoIdsJson,
                profile.getHourlyWage(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.getAvailableStartTime(),
                profile.getAvailableEndTime()
        );
        SitterProfile saved = sitterProfileRepository.save(profile);

        removeDetachedSitterImages(user.getId(), oldPhotoIds, request.profilePhotoIds());

        return toSitterProfileResponse(saved);
    }

    @Transactional
    public SitterImageUploadResponse uploadSitterProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        if (file.getSize() > 1_500_000L) {
            throw new IllegalArgumentException("이미지는 1.5MB 이하만 업로드할 수 있습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.SITTER) {
            throw new IllegalArgumentException("시터 계정만 이미지를 업로드할 수 있습니다.");
        }
        if (sitterProfileImageRepository.countByUserId(user.getId()) >= 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 업로드할 수 있습니다. 마이페이지에서 삭제 후 다시 시도해주세요.");
        }
        String id = UUID.randomUUID().toString();
        final byte[] data;
        try {
            data = file.getBytes();
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("파일을 읽는 중 오류가 발생했습니다.", e);
        }
        sitterProfileImageRepository.save(new SitterProfileImage(id, user.getId(), data, contentType));
        return new SitterImageUploadResponse(id, "public/sitter-images/" + id);
    }

    @Transactional
    public void deleteMySitterProfileImage(String id) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.SITTER) {
            throw new IllegalArgumentException("시터 계정만 이미지를 삭제할 수 있습니다.");
        }
        SitterProfileImage img = sitterProfileImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));
        if (!img.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 업로드한 이미지만 삭제할 수 있습니다.");
        }
        sitterProfileImageRepository.delete(img);
    }

    private void validateSitterProfileRequest(SitterProfileUpsertRequest request, Long userId) {
        for (String act : request.availableActivities()) {
            if (!SitterActivityCatalog.isAllowed(act)) {
                throw new IllegalArgumentException("허용되지 않은 활동입니다: " + act);
            }
        }
        if (request.availableActivities().size() != new LinkedHashSet<>(request.availableActivities()).size()) {
            throw new IllegalArgumentException("활동이 중복되었습니다.");
        }
        parseAgeGroups(request.preferredAgeGroups());

        boolean negotiable = Boolean.TRUE.equals(request.hourlyNegotiable());
        if (!negotiable) {
            Integer w = request.childcareHourlyWage();
            if (w == null || w < 1) {
                throw new IllegalArgumentException("보육·돌봄 희망 시급을 입력하거나, 협의 가능을 선택해주세요.");
            }
        } else if (request.childcareHourlyWage() != null && request.childcareHourlyWage() < 1) {
            throw new IllegalArgumentException("시급은 1원 이상이어야 합니다.");
        }

        Set<String> triples = new HashSet<>();
        for (PreferredRegionDto r : request.preferredRegions()) {
            String key = r.sido().trim() + "|" + r.sigungu().trim() + "|" + r.dong().trim();
            if (!triples.add(key)) {
                throw new IllegalArgumentException("동일한 희망 지역이 중복되었습니다.");
            }
        }

        for (String photoId : request.profilePhotoIds()) {
            SitterProfileImage img = sitterProfileImageRepository.findById(photoId)
                    .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이미지입니다: " + photoId));
            if (!img.getUserId().equals(userId)) {
                throw new IllegalArgumentException("다른 사용자의 이미지는 사용할 수 없습니다.");
            }
        }
    }

    private static List<SitterChildAgePreference> parseAgeGroups(List<String> raw) {
        List<SitterChildAgePreference> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String s : raw) {
            if (s == null || s.isBlank()) {
                continue;
            }
            SitterChildAgePreference v;
            try {
                v = SitterChildAgePreference.valueOf(s.trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("선호 연령대 값이 올바르지 않습니다: " + s);
            }
            if (!seen.add(v.name())) {
                throw new IllegalArgumentException("선호 연령대가 중복되었습니다.");
            }
            out.add(v);
        }
        return out;
    }

    private void removeDetachedSitterImages(Long userId, List<String> previousIds, List<String> newIds) {
        if (previousIds == null || previousIds.isEmpty()) {
            return;
        }
        for (String oldId : previousIds) {
            if (!newIds.contains(oldId)) {
                sitterProfileImageRepository.findById(oldId).filter(img -> img.getUserId().equals(userId))
                        .ifPresent(sitterProfileImageRepository::delete);
            }
        }
    }

    private SitterProfileResponse toSitterProfileResponse(SitterProfile profile) {
        return new SitterProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getAge(),
                profile.getGender(),
                profile.getPhoneNumber(),
                profile.getYearsOfExperience(),
                profile.getHasCertificate(),
                profile.getRegion(),
                profile.getBio(),
                profile.getNationalityType(),
                readStringList(profile.getAvailableActivitiesJson()),
                profile.getChildcareHourlyWage(),
                profile.getHourlyNegotiable(),
                profile.getCctvConsent(),
                readPreferredRegions(profile.getPreferredRegionsJson()),
                readAgeEnums(profile.getPreferredAgeGroupsJson()),
                readStringList(profile.getProfilePhotoIdsJson())
        );
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<PreferredRegionDto> readPreferredRegions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<SitterChildAgePreference> readAgeEnums(String json) {
        List<String> names = readStringList(json);
        List<SitterChildAgePreference> out = new ArrayList<>();
        for (String n : names) {
            try {
                out.add(SitterChildAgePreference.valueOf(n));
            } catch (Exception ignored) {
                // ignore unknown legacy values
            }
        }
        return out;
    }

    @Transactional
    public void deleteMyParentProfile() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.PARENT) {
            throw new IllegalArgumentException("부모 계정만 부모 프로필을 삭제할 수 있습니다.");
        }
        ParentProfile profile = parentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 부모 프로필이 존재하지 않습니다."));
        parentProfileRepository.delete(profile);
    }

    @Transactional
    public void deleteMySitterProfile() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() != UserRole.SITTER) {
            throw new IllegalArgumentException("시터 계정만 시터 프로필을 삭제할 수 있습니다.");
        }
        SitterProfile profile = sitterProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("삭제할 시터 프로필이 존재하지 않습니다."));
        sitterProfileImageRepository.deleteByUserId(user.getId());
        sitterProfileRepository.delete(profile);
    }
}
