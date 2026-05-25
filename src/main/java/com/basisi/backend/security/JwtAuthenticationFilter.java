package com.basisi.backend.security;

// 요청 필터 기반 인증 구현을 위한 OncePerRequestFilter 클래스입니다.
import jakarta.servlet.FilterChain;
// 서블릿 예외 타입입니다.
import jakarta.servlet.ServletException;
// HTTP 요청 객체 타입입니다.
import jakarta.servlet.http.HttpServletRequest;
// HTTP 응답 객체 타입입니다.
import jakarta.servlet.http.HttpServletResponse;
// 입출력 예외 타입입니다.
import java.io.IOException;
// 스프링 인증 토큰 타입입니다.
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// SecurityContext 저장소 접근을 위한 클래스입니다.
import org.springframework.security.core.context.SecurityContextHolder;
// UserDetails 타입입니다.
import org.springframework.security.core.userdetails.UserDetails;
// UserDetailsService 타입입니다.
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// 요청 필터 부모 클래스입니다.
import org.springframework.web.filter.OncePerRequestFilter;
// 스프링 컴포넌트 등록 어노테이션입니다.
import org.springframework.stereotype.Component;
// 웹 인증 세부 정보 생성을 위한 클래스입니다.
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

// 요청 헤더의 JWT를 검증하여 인증 컨텍스트를 세팅하는 필터입니다.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 생성/검증을 담당하는 컴포넌트입니다.
    private final JwtTokenProvider jwtTokenProvider;
    // 사용자 상세정보 조회를 담당하는 서비스입니다.
    private final UserDetailsService userDetailsService;

    // 생성자로 필요한 컴포넌트를 주입받습니다.
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        // 주입받은 JWT Provider를 필드에 저장합니다.
        this.jwtTokenProvider = jwtTokenProvider;
        // 주입받은 UserDetailsService를 필드에 저장합니다.
        this.userDetailsService = userDetailsService;
    }

    // 요청마다 JWT 인증 여부를 검사하는 핵심 메서드입니다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Authorization 헤더 값을 읽어옵니다.
        String authorizationHeader = request.getHeader("Authorization");
        // 헤더에서 Bearer 토큰 문자열을 추출합니다.
        String token = resolveToken(authorizationHeader);
        // EventSource는 커스텀 헤더를 쓰기 어려워 SSE 구독 URL에 access_token을 허용합니다.
        if ((token == null || token.isBlank()) && isNotificationStreamRequest(request)) {
            String qp = request.getParameter("access_token");
            if (qp != null && !qp.isBlank()) {
                token = qp;
            }
        }

        // 토큰이 존재하고 유효하면 인증 정보를 SecurityContext에 저장합니다.
        if (token != null && !token.isBlank() && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 사용자 이메일(subject)을 추출합니다.
            String email = jwtTokenProvider.getEmailFromToken(token);
            try {
                // 이메일로 사용자 상세정보를 조회합니다.
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 인증 객체를 생성합니다.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                // 요청 세부 정보를 인증 객체에 연결합니다.
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // SecurityContext에 인증 객체를 저장합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException ignored) {
                // DB가 비어 있거나 다른 서버의 토큰인 경우: 서명은 맞지만 사용자가 없음 → 비로그인과 동일하게 처리
            }
        }

        // 다음 필터 체인으로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }

    private boolean isNotificationStreamRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI() != null
                && request.getRequestURI().contains("/api/notifications/stream");
    }

    // Authorization 헤더에서 Bearer 토큰을 파싱합니다.
    private String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String h = authorizationHeader.trim();
        // 프록시/클라이언트에 따라 bearer 대소문자가 달라질 수 있음
        if (h.length() < 7 || !h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String raw = h.substring(7).trim();
        return raw.isEmpty() ? null : raw;
    }
}
