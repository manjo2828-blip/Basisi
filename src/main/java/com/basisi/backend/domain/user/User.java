package com.basisi.backend.domain.user;

// JPA 엔티티의 생성/수정 시각 자동 저장을 위한 어노테이션입니다.
import jakarta.persistence.Column;
// JPA 엔티티 선언을 위한 어노테이션입니다.
import jakarta.persistence.Entity;
// 기본 키 생성을 위한 전략 어노테이션입니다.
import jakarta.persistence.GeneratedValue;
// 기본 키 생성 전략 타입입니다.
import jakarta.persistence.GenerationType;
// 기본 키 필드를 선언하기 위한 어노테이션입니다.
import jakarta.persistence.Id;
// Enum 값을 문자열로 저장하기 위한 어노테이션입니다.
import jakarta.persistence.EnumType;
// Enum 저장 방식을 지정하는 어노테이션입니다.
import jakarta.persistence.Enumerated;
// 테이블 이름을 지정하기 위한 어노테이션입니다.
import jakarta.persistence.Table;
// 생성/수정 시각 타입입니다.
import java.time.LocalDateTime;
// Lombok 빌더 패턴 생성을 위한 어노테이션입니다.
import lombok.Builder;
// Getter 메서드 자동 생성을 위한 어노테이션입니다.
import lombok.Getter;
// 기본 생성자 자동 생성을 위한 어노테이션입니다.
import lombok.NoArgsConstructor;
// 모든 필드를 받는 생성자 자동 생성을 위한 어노테이션입니다.
import lombok.AllArgsConstructor;
// 엔티티 저장 직전 콜백을 위한 어노테이션입니다.
import jakarta.persistence.PrePersist;
// 엔티티 수정 직전 콜백을 위한 어노테이션입니다.
import jakarta.persistence.PreUpdate;

// 사용자 계정 정보를 저장하는 엔티티입니다.
@Entity
// users 테이블과 매핑합니다.
@Table(name = "users")
// 필드 조회용 getter를 자동 생성합니다.
@Getter
// JPA용 기본 생성자를 생성합니다.
@NoArgsConstructor
// 빌더/테스트 편의를 위한 전체 생성자를 생성합니다.
@AllArgsConstructor
public class User {

    // 사용자 고유 식별자 기본 키입니다.
    @Id
    // 데이터베이스 자동 증가 전략을 사용합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인에 사용하는 이메일입니다.
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 암호화된 비밀번호를 저장합니다.
    @Column(nullable = false, length = 255)
    private String password;

    // 사용자 표시 이름을 저장합니다.
    @Column(nullable = false, length = 50)
    private String name;

    // 사용자 권한(부모/시터)을 저장합니다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // 계정 생성 시각을 저장합니다.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 계정 수정 시각을 저장합니다.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 빌더 패턴으로 엔티티를 생성할 수 있게 합니다.
    @Builder
    public User(String email, String password, String name, UserRole role) {
        // 이메일 값을 설정합니다.
        this.email = email;
        // 암호화된 비밀번호 값을 설정합니다.
        this.password = password;
        // 사용자 이름 값을 설정합니다.
        this.name = name;
        // 사용자 권한 값을 설정합니다.
        this.role = role;
    }

    // 엔티티 최초 저장 직전에 생성/수정 시각을 자동 설정합니다.
    @PrePersist
    public void prePersist() {
        // 현재 시각을 생성 시각으로 저장합니다.
        this.createdAt = LocalDateTime.now();
        // 현재 시각을 수정 시각으로 저장합니다.
        this.updatedAt = LocalDateTime.now();
    }

    // 엔티티 수정 직전에 수정 시각을 자동 갱신합니다.
    @PreUpdate
    public void preUpdate() {
        // 현재 시각으로 수정 시각을 갱신합니다.
        this.updatedAt = LocalDateTime.now();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
