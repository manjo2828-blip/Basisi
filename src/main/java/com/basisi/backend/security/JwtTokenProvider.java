package com.basisi.backend.security;

// HMAC SHA 키 생성을 위한 JWT 클래스입니다.
import io.jsonwebtoken.io.Decoders;
// JWT 파서/빌더 생성 유틸 클래스입니다.
import io.jsonwebtoken.Jwts;
// JWT 서명 알고리즘 타입입니다.
import io.jsonwebtoken.SignatureAlgorithm;
// 시크릿 키 객체 타입입니다.
import javax.crypto.SecretKey;
// 키 생성 헬퍼 클래스입니다.
import io.jsonwebtoken.security.Keys;
// 날짜/시간 처리를 위한 클래스입니다.
import java.util.Date;
// 스프링 설정값 주입을 위한 어노테이션입니다.
import org.springframework.beans.factory.annotation.Value;
// 스프링 컴포넌트 등록 어노테이션입니다.
import org.springframework.stereotype.Component;

// JWT 생성 및 검증을 담당하는 컴포넌트입니다.
@Component
public class JwtTokenProvider {

    // application 설정에서 JWT 시크릿 값을 주입받습니다.
    @Value("${jwt.secret}")
    private String secret;

    // application 설정에서 Access Token 만료시간(ms)을 주입받습니다.
    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    // 문자열 시크릿을 JWT 서명 키 객체로 변환합니다.
    private SecretKey getSigningKey() {
        // Base64 인코딩된 시크릿 문자열을 바이트 배열로 디코딩합니다.
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        // HMAC 서명용 SecretKey 객체를 생성합니다.
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 사용자 식별 정보로 Access Token을 생성합니다.
    public String createAccessToken(Long userId, String email, String role) {
        // 현재 시간을 기준으로 토큰 만료 시각을 계산합니다.
        Date now = new Date();
        // 현재 시간에 만료 시간을 더해 만료 시각을 계산합니다.
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        // JWT를 생성하여 문자열 토큰으로 반환합니다.
        return Jwts.builder()
                // 토큰 주체(subject)에 사용자 이메일을 저장합니다.
                .subject(email)
                // 사용자 ID를 커스텀 클레임으로 저장합니다.
                .claim("userId", userId)
                // 사용자 역할을 커스텀 클레임으로 저장합니다.
                .claim("role", role)
                // 발급 시간을 저장합니다.
                .issuedAt(now)
                // 만료 시간을 저장합니다.
                .expiration(expiry)
                // 서명 키와 알고리즘을 지정합니다.
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                // 최종 JWT 문자열을 생성합니다.
                .compact();
    }

    // 전달된 토큰에서 사용자 이메일(subject)을 추출합니다.
    public String getEmailFromToken(String token) {
        // 토큰 파싱 후 subject를 반환합니다.
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 전달된 토큰이 서명/만료 기준으로 유효한지 검증합니다.
    public boolean validateToken(String token) {
        try {
            // 파싱이 성공하면 토큰이 유효하다고 판단합니다.
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            // 예외 없이 파싱되면 true를 반환합니다.
            return true;
        } catch (Exception ex) {
            // 파싱 중 예외가 발생하면 유효하지 않은 토큰으로 처리합니다.
            return false;
        }
    }
}
