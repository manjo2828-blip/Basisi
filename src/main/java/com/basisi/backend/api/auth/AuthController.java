package com.basisi.backend.api.auth;

// 인증 요청/응답 DTO import입니다.
import com.basisi.backend.api.auth.dto.AuthResponse;
import com.basisi.backend.api.auth.dto.LoginRequest;
import com.basisi.backend.api.auth.dto.SignUpRequest;
// 인증 서비스 import입니다.
import com.basisi.backend.service.AuthService;
// 요청 바디 검증을 위한 어노테이션입니다.
import jakarta.validation.Valid;
// HTTP 상태 코드를 표현하기 위한 enum입니다.
import org.springframework.http.HttpStatus;
// HTTP 응답 객체 생성을 위한 클래스입니다.
import org.springframework.http.ResponseEntity;
// REST 컨트롤러 선언 어노테이션입니다.
import org.springframework.web.bind.annotation.PostMapping;
// 요청 바디 매핑 어노테이션입니다.
import org.springframework.web.bind.annotation.RequestBody;
// 공통 URL prefix 지정 어노테이션입니다.
import org.springframework.web.bind.annotation.RequestMapping;
// JSON 응답 컨트롤러 선언 어노테이션입니다.
import org.springframework.web.bind.annotation.RestController;

// 회원가입/로그인 API를 제공하는 컨트롤러입니다.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 인증 비즈니스 로직을 처리하는 서비스입니다.
    private final AuthService authService;

    // 생성자로 인증 서비스를 주입받습니다.
    public AuthController(AuthService authService) {
        // 주입받은 인증 서비스를 필드에 저장합니다.
        this.authService = authService;
    }

    // 회원가입 API 엔드포인트입니다.
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        // 회원가입을 처리하고 토큰 포함 응답을 생성합니다.
        AuthResponse response = authService.signUp(request);
        // 201 Created 상태코드와 함께 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 로그인 API 엔드포인트입니다.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 로그인 검증 및 토큰 발급을 처리합니다.
        AuthResponse response = authService.login(request);
        // 200 OK 상태코드와 함께 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }
}
