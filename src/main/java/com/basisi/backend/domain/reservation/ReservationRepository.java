package com.basisi.backend.domain.reservation;

// 예약 엔티티를 위한 JPA 리포지토리입니다.
import java.util.List;
// 상태 목록 타입을 사용하기 위한 import입니다.
import java.util.Collection;
// 날짜/시간 타입을 사용하기 위한 import입니다.
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

// 예약 저장/조회 리포지토리입니다.
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 부모 프로필 기준으로 예약 목록을 조회합니다.
    List<Reservation> findByParentProfileIdOrderByCreatedAtDesc(Long parentProfileId);

    // 시터 프로필 기준으로 예약 목록을 조회합니다.
    List<Reservation> findBySitterProfileIdOrderByCreatedAtDesc(Long sitterProfileId);

    // 특정 시터의 예약 중, 주어진 시간 구간과 겹치는 예약이 존재하는지 확인합니다.
    // 겹침 조건: (기존.startAt < 요청.endAt) AND (기존.endAt > 요청.startAt)
    boolean existsBySitterProfileIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long sitterProfileId,
            Collection<ReservationStatus> statuses,
            LocalDateTime endAt,
            LocalDateTime startAt
    );

    // 특정 예약(id)을 제외하고, 시간 구간이 겹치는 예약이 존재하는지 확인합니다.
    boolean existsBySitterProfileIdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            Long sitterProfileId,
            Collection<ReservationStatus> statuses,
            LocalDateTime endAt,
            LocalDateTime startAt,
            Long id
    );
}

