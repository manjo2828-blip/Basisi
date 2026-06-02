package com.basisi.backend.api.common;

// 필드 검증 오류 정보를 사용하기 위한 클래스입니다.
import org.springframework.validation.FieldError;
// HTTP 상태 코드 enum입니다.
import org.springframework.http.HttpStatus;
// HTTP 응답 객체 클래스입니다.
import org.springframework.http.ResponseEntity;
// 검증 실패 예외 타입입니다.
import org.springframework.web.bind.MethodArgumentNotValidException;
// 전역 예외 처리를 위한 어노테이션입니다.
import org.springframework.web.bind.annotation.ExceptionHandler;
// REST 형태 전역 예외 핸들러 어노테이션입니다.
import org.springframework.web.bind.annotation.RestControllerAdvice;
// 로깅을 위한 SLF4J Logger 입니다.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 오류 응답에 사용할 맵 자료구조입니다.
import java.util.HashMap;
import java.util.Map;

// 애플리케이션 전역 예외를 JSON 형태로 변환하는 핸들러입니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 잘못된 요청 파라미터 예외를 400으로 처리합니다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // 오류 메시지를 담을 응답 맵을 생성합니다.
        Map<String, String> body = new HashMap<>();
        // 오류 타입 정보를 저장합니다.
        body.put("error", "BAD_REQUEST");
        // 상세 오류 메시지를 저장합니다.
        body.put("message", ex.getMessage());
        // 400 상태코드와 함께 오류 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // @Valid 검증 실패 예외를 400으로 처리합니다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        // 첫 번째 필드 검증 오류를 추출합니다.
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        // 오류 메시지를 담을 응답 맵을 생성합니다.
        Map<String, String> body = new HashMap<>();
        // 오류 타입 정보를 저장합니다.
        body.put("error", "VALIDATION_FAILED");
        // 기본 메시지가 있으면 해당 메시지를, 없으면 기본 문구를 저장합니다.
        body.put("message", fieldError != null ? fieldError.getDefaultMessage() : "요청값 검증에 실패했습니다.");
        // 400 상태코드와 함께 오류 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 그 외 모든 예외를 잡아 500 JSON 으로 변환합니다. (Render 환경에서 빈 본문 응답을 막기 위해)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllUnexpected(Exception ex) {
        log.error("[Basisi] Unhandled exception: {}", ex.getMessage(), ex);
        Map<String, String> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
