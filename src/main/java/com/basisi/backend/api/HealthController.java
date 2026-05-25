package com.basisi.backend.api;

// Map 자료구조를 사용하기 위한 클래스입니다.
import java.util.Map;
// REST 컨트롤러를 선언하기 위한 어노테이션입니다.
import org.springframework.web.bind.annotation.GetMapping;
// 요청 경로의 공통 prefix를 지정하는 어노테이션입니다.
import org.springframework.web.bind.annotation.RequestMapping;
// JSON 형태 응답을 위한 컨트롤러 어노테이션입니다.
import org.springframework.web.bind.annotation.RestController;

// 서버 상태를 점검하기 위한 헬스체크 컨트롤러입니다.
@RestController
@RequestMapping("/api")
public class HealthController {

    // 서버가 정상 동작 중인지 확인하는 헬스체크 API입니다.
    @GetMapping("/health")
    public Map<String, String> health() {
        // 상태 정보를 간단한 JSON 형태로 반환합니다.
        return Map.of(
                "status", "UP",
                "service", "basisi-backend"
        );
    }
}
