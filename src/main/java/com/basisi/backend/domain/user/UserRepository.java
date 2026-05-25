package com.basisi.backend.domain.user;

// Optional 반환 타입을 사용하기 위한 클래스입니다.
import java.util.Optional;
// JPA Repository 기본 기능을 사용하기 위한 인터페이스입니다.
import org.springframework.data.jpa.repository.JpaRepository;

// 사용자 엔티티 저장/조회 기능을 제공하는 리포지토리입니다.
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 정보를 조회합니다. (JWT subject·로그인 입력과 DB 저장 대소문자 차이 허용)
    Optional<User> findByEmailIgnoreCase(String email);

    // 이메일 중복 여부를 확인합니다.
    boolean existsByEmailIgnoreCase(String email);
}
