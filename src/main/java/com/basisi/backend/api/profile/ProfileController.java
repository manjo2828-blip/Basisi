package com.basisi.backend.api.profile;

// 프로필 DTO import입니다.
import com.basisi.backend.api.profile.dto.ParentProfileResponse;
import com.basisi.backend.api.profile.dto.ParentProfileUpsertRequest;
import com.basisi.backend.api.profile.dto.SitterImageUploadResponse;
import com.basisi.backend.api.profile.dto.SitterProfileResponse;
import com.basisi.backend.api.profile.dto.SitterProfileUpsertRequest;
// 프로필 서비스 import입니다.
import com.basisi.backend.service.ProfileService;
// 요청값 검증을 위한 어노테이션입니다.
import jakarta.validation.Valid;
// HTTP 응답 객체 생성을 위한 클래스입니다.
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// REST 컨트롤러 선언 어노테이션입니다.
import org.springframework.web.bind.annotation.GetMapping;
// DELETE 메서드 매핑 어노테이션입니다.
import org.springframework.web.bind.annotation.DeleteMapping;
// PathVariable import입니다.
import org.springframework.web.bind.annotation.PathVariable;
// 요청 바디 매핑 어노테이션입니다.
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
// 공통 URL prefix 지정 어노테이션입니다.
import org.springframework.web.bind.annotation.RequestMapping;
// JSON 응답 컨트롤러 선언 어노테이션입니다.
import org.springframework.web.bind.annotation.RestController;
// PUT 메서드 매핑 어노테이션입니다.
import org.springframework.web.bind.annotation.PutMapping;
// POST 메서드 매핑 어노테이션입니다.
import org.springframework.web.bind.annotation.PostMapping;
// MultipartFile import입니다.
import org.springframework.web.multipart.MultipartFile;

// 내 프로필(부모/시터) CRUD API를 제공하는 컨트롤러입니다.
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    // 프로필 비즈니스 로직을 처리하는 서비스입니다.
    private final ProfileService profileService;

    // 생성자로 프로필 서비스를 주입받습니다.
    public ProfileController(ProfileService profileService) {
        // 주입받은 서비스를 필드에 저장합니다.
        this.profileService = profileService;
    }

    // 현재 로그인한 부모 사용자의 프로필을 조회합니다.
    @GetMapping("/parent/me")
    public ResponseEntity<ParentProfileResponse> getMyParentProfile() {
        // 서비스에서 내 부모 프로필을 조회합니다.
        ParentProfileResponse response = profileService.getMyParentProfile();
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 부모 사용자의 프로필을 생성/수정합니다.
    @PutMapping("/parent/me")
    public ResponseEntity<ParentProfileResponse> upsertMyParentProfile(@Valid @RequestBody ParentProfileUpsertRequest request) {
        // 서비스에서 내 부모 프로필을 저장합니다.
        ParentProfileResponse response = profileService.upsertMyParentProfile(request);
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 시터 사용자의 프로필을 조회합니다.
    @GetMapping("/sitter/me")
    public ResponseEntity<SitterProfileResponse> getMySitterProfile() {
        // 서비스에서 내 시터 프로필을 조회합니다.
        SitterProfileResponse response = profileService.getMySitterProfile();
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 시터 사용자의 프로필을 생성/수정합니다.
    @PutMapping("/sitter/me")
    public ResponseEntity<SitterProfileResponse> upsertMySitterProfile(@Valid @RequestBody SitterProfileUpsertRequest request) {
        // 서비스에서 내 시터 프로필을 저장합니다.
        SitterProfileResponse response = profileService.upsertMySitterProfile(request);
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/sitter/me/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SitterImageUploadResponse> uploadSitterProfileImage(@RequestParam("file") MultipartFile file) {
        SitterImageUploadResponse response = profileService.uploadSitterProfileImage(file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sitter/me/images/{id}")
    public ResponseEntity<Void> deleteSitterProfileImage(@PathVariable String id) {
        profileService.deleteMySitterProfileImage(id);
        return ResponseEntity.noContent().build();
    }

    // 현재 로그인한 부모 사용자의 프로필을 삭제합니다.
    @DeleteMapping("/parent/me")
    public ResponseEntity<Void> deleteMyParentProfile() {
        // 서비스에서 내 부모 프로필을 삭제합니다.
        profileService.deleteMyParentProfile();
        // 204 No Content로 응답을 반환합니다.
        return ResponseEntity.noContent().build();
    }

    // 현재 로그인한 시터 사용자의 프로필을 삭제합니다.
    @DeleteMapping("/sitter/me")
    public ResponseEntity<Void> deleteMySitterProfile() {
        // 서비스에서 내 시터 프로필을 삭제합니다.
        profileService.deleteMySitterProfile();
        // 204 No Content로 응답을 반환합니다.
        return ResponseEntity.noContent().build();
    }
}

