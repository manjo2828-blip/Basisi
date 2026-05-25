package com.basisi.backend.domain.reservation;

// 부모 프로필을 참조하기 위한 import입니다.
import com.basisi.backend.domain.profile.ParentProfile;
// 시터 프로필을 참조하기 위한 import입니다.
import com.basisi.backend.domain.profile.SitterProfile;
// JPA 컬럼 어노테이션입니다.
import jakarta.persistence.Column;
// JPA 엔티티 어노테이션입니다.
import jakarta.persistence.Entity;
// 기본 키 생성 어노테이션입니다.
import jakarta.persistence.GeneratedValue;
// 기본 키 생성 전략입니다.
import jakarta.persistence.GenerationType;
// 기본 키 어노테이션입니다.
import jakarta.persistence.Id;
// 조인 컬럼 어노테이션입니다.
import jakarta.persistence.JoinColumn;
// 다대일 연관관계 어노테이션입니다.
import jakarta.persistence.ManyToOne;
// Enum 저장 방식 어노테이션입니다.
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
// 테이블 이름 지정 어노테이션입니다.
import jakarta.persistence.Table;
// 지연 로딩을 위한 fetch 타입입니다.
import jakarta.persistence.FetchType;
// 생성/수정 시각 타입입니다.
import java.time.LocalDateTime;
// 생성/수정 콜백 어노테이션입니다.
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
// Lombok Getter 어노테이션입니다.
import lombok.Getter;
// Lombok 기본 생성자 어노테이션입니다.
import lombok.NoArgsConstructor;

// 예약 정보를 저장하는 엔티티입니다.
@Entity
// reservations 테이블과 매핑합니다.
@Table(name = "reservations")
// 필드 조회용 getter를 생성합니다.
@Getter
// JPA 기본 생성자를 생성합니다.
@NoArgsConstructor
public class Reservation {

    // 예약 고유 식별자입니다.
    @Id
    // 자동 증가 전략을 사용합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약을 신청한 부모 프로필입니다.
    @ManyToOne(fetch = FetchType.LAZY)
    // parent_profile_id로 조인합니다.
    @JoinColumn(name = "parent_profile_id", nullable = false)
    private ParentProfile parentProfile;

    // 예약을 받은 시터 프로필입니다.
    @ManyToOne(fetch = FetchType.LAZY)
    // sitter_profile_id로 조인합니다.
    @JoinColumn(name = "sitter_profile_id", nullable = false)
    private SitterProfile sitterProfile;

    // 돌봄 시작 시각입니다.
    @Column(nullable = false)
    private LocalDateTime startAt;

    // 돌봄 종료 시각입니다.
    @Column(nullable = false)
    private LocalDateTime endAt;

    // 부모가 남기는 요청 메모입니다.
    @Column(nullable = true, length = 500)
    private String note;

    // 예약 상태입니다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    // 예약 생성 시각입니다.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 예약 수정 시각입니다.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 예약을 생성합니다.
    public Reservation(ParentProfile parentProfile, SitterProfile sitterProfile, LocalDateTime startAt, LocalDateTime endAt, String note) {
        // 부모 프로필을 설정합니다.
        this.parentProfile = parentProfile;
        // 시터 프로필을 설정합니다.
        this.sitterProfile = sitterProfile;
        // 시작 시각을 설정합니다.
        this.startAt = startAt;
        // 종료 시각을 설정합니다.
        this.endAt = endAt;
        // 메모를 설정합니다.
        this.note = note;
        // 최초 상태를 REQUESTED로 설정합니다.
        this.status = ReservationStatus.REQUESTED;
    }

    // 시터가 예약을 수락합니다.
    public void accept() {
        // REQUESTED 상태에서만 수락 가능합니다.
        if (this.status != ReservationStatus.REQUESTED) {
            // 상태가 올바르지 않으면 예외를 발생시킵니다.
            throw new IllegalStateException("요청된 예약만 수락할 수 있습니다.");
        }
        // 상태를 ACCEPTED로 변경합니다.
        this.status = ReservationStatus.ACCEPTED;
    }

    // 시터가 예약을 거절합니다.
    public void reject() {
        // REQUESTED 상태에서만 거절 가능합니다.
        if (this.status != ReservationStatus.REQUESTED) {
            // 상태가 올바르지 않으면 예외를 발생시킵니다.
            throw new IllegalStateException("요청된 예약만 거절할 수 있습니다.");
        }
        // 상태를 REJECTED로 변경합니다.
        this.status = ReservationStatus.REJECTED;
    }

    // 부모가 예약을 취소합니다.
    public void cancel() {
        // 이미 완료된 상태(거절/취소)면 다시 취소할 수 없습니다.
        if (this.status == ReservationStatus.REJECTED || this.status == ReservationStatus.CANCELED) {
            // 상태가 올바르지 않으면 예외를 발생시킵니다.
            throw new IllegalStateException("이미 종료된 예약은 취소할 수 없습니다.");
        }
        // 상태를 CANCELED로 변경합니다.
        this.status = ReservationStatus.CANCELED;
    }

    // 최초 저장 시 생성/수정 시각을 자동 설정합니다.
    @PrePersist
    public void prePersist() {
        // 현재 시각을 생성 시각으로 저장합니다.
        this.createdAt = LocalDateTime.now();
        // 현재 시각을 수정 시각으로 저장합니다.
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 시 수정 시각을 자동 갱신합니다.
    @PreUpdate
    public void preUpdate() {
        // 현재 시각으로 수정 시각을 갱신합니다.
        this.updatedAt = LocalDateTime.now();
    }
}

