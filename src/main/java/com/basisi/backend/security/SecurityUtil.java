package com.basisi.backend.security;

// 인증 컨텍스트에서 사용자 정보를 꺼내기 위한 클래스입니다.
import org.springframework.security.core.Authentication;
// 인증 컨텍스트 홀더 클래스입니다.
import org.springframework.security.core.context.SecurityContextHolder;
// 문자열 유효성 검사를 위한 유틸입니다.
import org.springframework.util.StringUtils;

// 현재 로그인 사용자의 정보를 가져오는 유틸 클래스입니다.
public final class SecurityUtil {

    // 유틸 클래스는 인스턴스 생성을 막습니다.
    private SecurityUtil() {
        // 외부에서 생성하지 못하도록 private 생성자입니다.
    }

    // 현재 인증된 사용자의 이메일(Username)을 반환합니다.
    public static String getCurrentUserEmail() {
        // SecurityContext에서 Authentication 객체를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 인증 정보가 없으면 예외를 던집니다.
        if (authentication == null) {
            // 인증 정보가 없는 상태를 명확히 알리기 위한 예외입니다.
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // 인증 주체 이름(우리 구현에서는 이메일)을 가져옵니다.
        String name = authentication.getName();
        // 이름이 비어 있으면 예외를 던집니다.
        if (!StringUtils.hasText(name)) {
            // 빈 사용자 정보를 방어합니다.
            throw new IllegalStateException("인증 사용자 정보가 비어 있습니다.");
        }
        // 이메일(Username)을 반환합니다.
        return name;
    }
}
