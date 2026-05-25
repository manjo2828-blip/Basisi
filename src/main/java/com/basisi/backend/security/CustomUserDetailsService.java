package com.basisi.backend.security;

// 사용자 리포지토리를 사용하기 위한 import입니다.
import com.basisi.backend.domain.user.UserRepository;
// 스프링 보안 UserDetails 타입입니다.
import org.springframework.security.core.userdetails.UserDetails;
// 사용자 없음 예외 타입입니다.
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// UserDetailsService 인터페이스입니다.
import org.springframework.security.core.userdetails.UserDetailsService;
// 스프링 컴포넌트 등록 어노테이션입니다.
import org.springframework.stereotype.Service;

// 이메일 기반 사용자 조회를 담당하는 UserDetailsService 구현체입니다.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // 사용자 조회를 위한 리포지토리입니다.
    private final UserRepository userRepository;

    // 생성자로 리포지토리를 주입받습니다.
    public CustomUserDetailsService(UserRepository userRepository) {
        // 주입받은 리포지토리를 필드에 저장합니다.
        this.userRepository = userRepository;
    }

    // 이메일(username)로 사용자를 조회해 Security UserDetails로 변환합니다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이메일로 사용자 엔티티를 조회합니다.
        com.basisi.backend.domain.user.User user = userRepository.findByEmailIgnoreCase(username)
                // 사용자가 없으면 인증 예외를 던집니다.
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 스프링 시큐리티 기본 UserDetails 객체로 변환하여 반환합니다.
        return org.springframework.security.core.userdetails.User.builder()
                // 인증 주체로 이메일을 사용합니다.
                .username(user.getEmail())
                // 저장된 암호화 비밀번호를 설정합니다.
                .password(user.getPassword())
                // 권한 이름은 ROLE_ 접두사 규칙에 맞게 부여합니다.
                .roles(user.getRole().name())
                // 생성된 UserDetails를 반환합니다.
                .build();
    }
}
