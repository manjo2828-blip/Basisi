package com.basisi.backend.api.sitter;

// 공개 상세 DTO import입니다.
import com.basisi.backend.api.sitter.dto.SitterPublicDetailResponse;
// 탐색 응답 DTO import입니다.
import com.basisi.backend.api.sitter.dto.SitterSearchResponse;
// 탐색 서비스 import입니다.
import com.basisi.backend.service.SitterSearchService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
// HTTP 응답 객체 import입니다.
import org.springframework.http.ResponseEntity;
// 요청 파라미터 매핑 어노테이션입니다.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 시터 탐색 API를 제공하는 컨트롤러입니다.
@RestController
@RequestMapping("/api/sitters")
public class SitterSearchController {

    // 시터 탐색 비즈니스 로직 서비스입니다.
    private final SitterSearchService sitterSearchService;

    // 생성자로 서비스를 주입받습니다.
    public SitterSearchController(SitterSearchService sitterSearchService) {
        // 주입받은 서비스를 필드에 저장합니다.
        this.sitterSearchService = sitterSearchService;
    }

    // 내 주변 시터 탐색 API입니다. (기존 프론트 호환 List 응답)
    @GetMapping("/search")
    public ResponseEntity<List<SitterSearchResponse>> search(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minYearsOfExperience
    ) {
        // 기존 형식(List)으로 탐색 결과를 가져옵니다.
        List<SitterSearchResponse> result = sitterSearchService.searchLegacy(region, gender, minYearsOfExperience);
        // 200 OK로 결과를 반환합니다.
        return ResponseEntity.ok(result);
    }

    // 확장된 내 주변 시터 탐색 API입니다. (페이지네이션/정렬/복합필터)
    @GetMapping("/search/page")
    public ResponseEntity<Page<SitterSearchResponse>> searchPage(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minYearsOfExperience,
            // 페이징/정렬 파라미터입니다. (page, size, sort)
            @PageableDefault(size = 10) Pageable pageable
    ) {
        // 확장 형식(Page)으로 탐색 결과를 가져옵니다.
        Page<SitterSearchResponse> result = sitterSearchService.search(
                region,
                gender,
                minYearsOfExperience,
                pageable
        );
        // 200 OK로 결과를 반환합니다.
        return ResponseEntity.ok(result);
    }

    // 지도/목록 클릭 시 사용하는 공개 시터 상세 API입니다.
    @GetMapping("/{sitterProfileId}")
    public ResponseEntity<SitterPublicDetailResponse> getPublicSitterDetail(
            // 시터 프로필 ID입니다.
            @PathVariable Long sitterProfileId
    ) {
        // 공개 상세 정보를 조회합니다.
        SitterPublicDetailResponse response = sitterSearchService.getPublicSitterDetail(sitterProfileId);
        // 200 OK로 결과를 반환합니다.
        return ResponseEntity.ok(response);
    }
}

