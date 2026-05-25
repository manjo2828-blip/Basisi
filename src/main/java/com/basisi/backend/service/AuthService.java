package com.basisi.backend.service;

// 인증 관련 요청/응답 DTO import입니다.
import com.basisi.backend.api.auth.dto.AuthResponse;
import com.basisi.backend.api.auth.dto.LoginRequest;
import com.basisi.backend.api.auth.dto.SignUpRequest;
// 사용자 엔티티/리포지토리 import입니다.
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.domain.user.UserRole;
// JWT 발급 컴포넌트 import입니다.
import com.basisi.backend.security.JwtTokenProvider;
// 비밀번호 암호화/검증을 위한 인코더 인터페이스입니다.
import org.springframework.security.crypto.password.PasswordEncoder;
// 서비스 컴포넌트 등록 어노테이션입니다.
import org.springframework.stereotype.Service;
// 트랜잭션 처리를 위한 어노테이션입니다.
import org.springframework.transaction.annotation.Transactional;
// 요청값 검증 어노테이션입니다.
import org.springframework.util.StringUtils;

// 회원가입/로그인 비즈니스 로직을 담당하는 서비스입니다.
@Service
public class AuthService {

    // 사용자 저장/조회 리포지토리입니다.
    private final UserRepository userRepository;
    // 비밀번호 암호화/검증 인코더입니다.
    private final PasswordEncoder passwordEncoder;
    // JWT 발급 컴포넌트입니다.
    private final JwtTokenProvider jwtTokenProvider;

    // 생성자로 필요한 컴포넌트를 주입받습니다.
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        // 주입받은 사용자 리포지토리를 필드에 저장합니다.
        this.userRepository = userRepository;
        // 주입받은 비밀번호 인코더를 필드에 저장합니다.
        this.passwordEncoder = passwordEncoder;
        // 주입받은 JWT Provider를 필드에 저장합니다.
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 회원가입을 처리하고 JWT를 발급하여 반환합니다.
    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        // 이메일 중복 여부를 확인합니다.
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            // 중복 이메일이면 예외를 던집니다.
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        // 역할이 비어 있으면 기본값을 부모(PARENT)로 지정합니다.
        UserRole role = request.role() == null ? UserRole.PARENT : request.role();
        // 이름이 공백인 경우를 추가로 방어합니다.
        if (!StringUtils.hasText(request.name())) {
            // 이름이 유효하지 않으면 예외를 던집니다.
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        // 회원가입 요청 데이터를 사용자 엔티티로 변환합니다.
        User user = User.builder()
                // 이메일을 설정합니다.
                .email(request.email())
                // 비밀번호를 BCrypt로 암호화하여 저장합니다.
                .password(passwordEncoder.encode(request.password()))
                // 사용자 이름을 설정합니다.
                .name(request.name())
                // 사용자 역할을 설정합니다.
                .role(role)
                // 엔티티 객체를 생성합니다.
                .build();

        // 데이터베이스에 사용자 정보를 저장합니다.
        User savedUser = userRepository.save(user);
        // 저장된 사용자 정보로 Access Token을 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        // 회원가입 성공 응답을 반환합니다.
        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().name(),
                accessToken
        );
    }

    // 로그인 요청을 검증하고 JWT를 발급하여 반환합니다.
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 이메일로 사용자 정보를 조회합니다.
        User user = userRepository.findByEmailIgnoreCase(request.email())
                // 존재하지 않으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 입력 비밀번호와 저장된 암호화 비밀번호가 일치하는지 확인합니다.
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            // 비밀번호가 일치하지 않으면 예외를 던집니다.
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 로그인 성공 시 Access Token을 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 로그인 성공 응답을 반환합니다.
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                accessToken
        );
    }
}
