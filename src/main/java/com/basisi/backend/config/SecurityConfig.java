package com.basisi.backend.config;

// JWT 인증 필터를 주입받기 위한 클래스입니다.
import com.basisi.backend.security.JwtAuthenticationFilter;
// CORS 설정을 위한 클래스입니다.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
// Spring 컨텍스트에 보안 설정 빈을 등록하기 위한 어노테이션입니다.
import org.springframework.context.annotation.Bean;
// 자바 기반 설정 클래스를 선언하기 위한 어노테이션입니다.
import org.springframework.context.annotation.Configuration;
// CORS 설정 객체입니다.
import org.springframework.web.cors.CorsConfiguration;
// CORS 설정 소스 인터페이스입니다.
import org.springframework.web.cors.CorsConfigurationSource;
// URL 패턴별 CORS 설정을 등록하는 클래스입니다.
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// 비밀번호 단방향 암호화를 위한 인코더입니다.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// PasswordEncoder 인터페이스 타입입니다.
import org.springframework.security.crypto.password.PasswordEncoder;
// Spring Security HTTP 보안 설정 객체입니다.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// 세션을 사용하지 않는 STATELESS 정책을 설정하기 위한 enum입니다.
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
// UsernamePasswordAuthenticationFilter 앞에 필터를 추가하기 위한 클래스입니다.
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// SecurityFilterChain 타입을 사용하기 위한 클래스입니다.
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpStatus;

// 보안 관련 설정을 담당하는 구성 클래스입니다.
@Configuration
public class SecurityConfig {

    // JWT 인증 필터를 주입받아 요청마다 토큰 검증에 사용합니다.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Render 같은 배포 환경의 프론트엔드 도메인을 환경변수 BASISI_ALLOWED_ORIGINS 로 콤마 구분 추가합니다.
    // 예) BASISI_ALLOWED_ORIGINS=https://basisi-frontend.onrender.com,https://basisi.example.com
    @Value("${basisi.allowed-origins:}")
    private String allowedOriginsProperty;

    // 생성자를 통해 JWT 인증 필터를 주입받습니다.
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        // 주입받은 JWT 인증 필터를 필드에 저장합니다.
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // 회원가입 시 비밀번호 암호화를 위한 인코더 빈을 등록합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 알고리즘 기반 비밀번호 인코더를 반환합니다.
        return new BCryptPasswordEncoder();
    }

    // 프론트엔드 개발 서버에서 API 호출이 가능하도록 CORS를 설정합니다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // CORS 설정 객체를 생성합니다.
        CorsConfiguration configuration = new CorsConfiguration();
        // 로컬 개발 + 배포 도메인 모두 허용합니다.
        List<String> patterns = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                // Render Static/Web Service 기본 도메인을 와일드카드로 허용합니다.
                "https://*.onrender.com"
        ));
        // 환경변수 BASISI_ALLOWED_ORIGINS (콤마 구분) 로 추가 도메인을 등록합니다.
        if (allowedOriginsProperty != null && !allowedOriginsProperty.isBlank()) {
            for (String origin : Arrays.asList(allowedOriginsProperty.split(","))) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    patterns.add(trimmed);
                }
            }
        }
        configuration.setAllowedOriginPatterns(patterns);
        // 허용할 HTTP 메서드를 지정합니다.
        // OPTIONS는 브라우저 CORS 사전요청(preflight)에 필요합니다.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // fetch가 보내는 헤더가 환경마다 달라 * 로 허용(allowCredentials=false 이므로 안전).
        configuration.setAllowedHeaders(List.of("*"));
        // 브라우저가 Authorization 헤더를 노출하도록 설정합니다.
        configuration.setExposedHeaders(List.of("Authorization"));
        // 쿠키 기반 인증을 사용하지 않지만, 확장을 위해 false로 둡니다.
        configuration.setAllowCredentials(false);

        // URL 패턴별로 CORS 설정을 등록할 소스를 생성합니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 동일한 CORS 정책을 적용합니다.
        source.registerCorsConfiguration("/**", configuration);
        // CORS 설정 소스를 반환합니다.
        return source;
    }

    // 기본 보안 정책을 정의하는 필터 체인을 생성합니다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS 설정을 활성화합니다.
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // 현재 초기 개발 단계에서는 CSRF를 비활성화합니다.
        http.csrf(csrf -> csrf.disable());
        // 서버 세션을 사용하지 않고 JWT 기반으로 인증하기 위해 Stateless로 설정합니다.
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 미인증 요청은 403이 아닌 401로 응답해 프론트가 재로그인을 유도할 수 있게 합니다.
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        // Swagger 및 헬스체크 API는 인증 없이 접근 가능하게 허용합니다.
        http.authorizeHttpRequests(auth -> auth
                // Authorization 헤더가 있는 GET도 사전요청(OPTIONS)이 먼저 오므로 전 경로 OPTIONS 허용.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        "/api/auth/**",
                        "/api/health",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()
                // 공개 시터 탐색/상세·리뷰·점수 조회(GET) — AntPath로 명시(환경에 따라 method 매칭이 어긋나는 경우 방지).
                .requestMatchers(new AntPathRequestMatcher("/api/sitters/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/reviews/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/sitter-scores/sitters/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/public/**")).permitAll()
                // 나머지 요청은 JWT 인증이 필요하도록 설정합니다.
                .anyRequest().authenticated()
        );
        // UsernamePasswordAuthenticationFilter 전에 JWT 필터를 실행하도록 등록합니다.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // 구성된 보안 필터 체인을 반환합니다.
        return http.build();
    }
}
