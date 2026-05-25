package com.basisi.backend.domain.profile;

// Optional 타입을 사용하기 위한 import입니다.
import java.util.Optional;
// JPA 기본 리포지토리를 사용하기 위한 import입니다.
import org.springframework.data.jpa.repository.JpaRepository;

// 부모 프로필 저장/조회 리포지토리입니다.
public interface ParentProfileRepository extends JpaRepository<ParentProfile, Long> {

    // userId로 부모 프로필을 조회합니다.
    Optional<ParentProfile> findByUserId(Long userId);

    // userId로 부모 프로필 존재 여부를 확인합니다.
    boolean existsByUserId(Long userId);
}

