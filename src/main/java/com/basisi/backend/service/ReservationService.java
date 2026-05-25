package com.basisi.backend.service;

import com.basisi.backend.async.AsyncEventPublisher;
// 예약 DTO import입니다.
import com.basisi.backend.api.reservation.dto.ReservationCreateRequest;
import com.basisi.backend.api.reservation.dto.ReservationResponse;
// 프로필 도메인 import입니다.
import com.basisi.backend.domain.profile.ParentProfile;
import com.basisi.backend.domain.profile.ParentProfileRepository;
import com.basisi.backend.domain.profile.SitterProfile;
import com.basisi.backend.domain.profile.SitterProfileRepository;
// 예약 도메인 import입니다.
import com.basisi.backend.domain.reservation.Reservation;
import com.basisi.backend.domain.reservation.ReservationRepository;
import com.basisi.backend.domain.reservation.ReservationStatus;
import com.basisi.backend.domain.review.ReviewRepository;
// 사용자 도메인 import입니다.
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.domain.user.UserRole;
// 현재 사용자 이메일 유틸 import입니다.
import com.basisi.backend.security.SecurityUtil;
// 리스트 타입 import입니다.
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
// 서비스 어노테이션입니다.
import org.springframework.stereotype.Service;
// 트랜잭션 어노테이션입니다.
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// 예약 신청/수락/거절/취소 비즈니스 로직을 담당하는 서비스입니다.
@Service
public class ReservationService {

    // 사용자 조회 리포지토리입니다.
    private final UserRepository userRepository;
    // 부모 프로필 리포지토리입니다.
    private final ParentProfileRepository parentProfileRepository;
    // 시터 프로필 리포지토리입니다.
    private final SitterProfileRepository sitterProfileRepository;
    // 예약 리포지토리입니다.
    private final ReservationRepository reservationRepository;
    // 리뷰 리포지토리입니다.
    private final ReviewRepository reviewRepository;
    // 분산 락 처리 서비스입니다.
    private final ReservationLockService reservationLockService;
    // 예약 실시간 알림(SSE) 전송 서비스입니다.
    private final ReservationNotificationService reservationNotificationService;
    // 비동기 이벤트 발행기입니다.
    private final AsyncEventPublisher asyncEventPublisher;
    // REQUESTED 상태 겹침 허용 여부 정책입니다.
    private final boolean allowRequestedOverlap;
    // 최소 예약 시간(분) 정책입니다.
    private final long minimumDurationMinutes;

    // 생성자로 필요한 리포지토리를 주입받습니다.
    public ReservationService(
            UserRepository userRepository,
            ParentProfileRepository parentProfileRepository,
            SitterProfileRepository sitterProfileRepository,
            ReservationRepository reservationRepository,
            ReviewRepository reviewRepository,
            ReservationLockService reservationLockService,
            ReservationNotificationService reservationNotificationService,
            AsyncEventPublisher asyncEventPublisher,
            @Value("${reservation.policy.allow-requested-overlap:true}") boolean allowRequestedOverlap,
            @Value("${reservation.policy.minimum-duration-minutes:60}") long minimumDurationMinutes
    ) {
        // 사용자 리포지토리를 저장합니다.
        this.userRepository = userRepository;
        // 부모 프로필 리포지토리를 저장합니다.
        this.parentProfileRepository = parentProfileRepository;
        // 시터 프로필 리포지토리를 저장합니다.
        this.sitterProfileRepository = sitterProfileRepository;
        // 예약 리포지토리를 저장합니다.
        this.reservationRepository = reservationRepository;
        // 리뷰 리포지토리를 저장합니다.
        this.reviewRepository = reviewRepository;
        // 분산 락 서비스를 저장합니다.
        this.reservationLockService = reservationLockService;
        // 실시간 알림 서비스를 저장합니다.
        this.reservationNotificationService = reservationNotificationService;
        // 비동기 이벤트 발행기를 저장합니다.
        this.asyncEventPublisher = asyncEventPublisher;
        // REQUESTED 상태 겹침 허용 여부를 저장합니다.
        this.allowRequestedOverlap = allowRequestedOverlap;
        // 최소 예약 시간을 저장합니다.
        this.minimumDurationMinutes = minimumDurationMinutes;
    }

    // 부모가 예약을 신청합니다.
    @Transactional
    public ReservationResponse requestReservation(ReservationCreateRequest request) {
        // 현재 로그인한 사용자 이메일을 가져옵니다.
        String email = SecurityUtil.getCurrentUserEmail();
        // 사용자 정보를 조회합니다.
        User user = userRepository.findByEmailIgnoreCase(email)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 부모만 예약을 신청할 수 있습니다.
        if (user.getRole() != UserRole.PARENT) {
            // 역할이 다르면 예외를 던집니다.
            throw new IllegalArgumentException("부모 계정만 예약을 신청할 수 있습니다.");
        }
        // 부모 프로필을 조회합니다.
        ParentProfile parentProfile = parentProfileRepository.findByUserId(user.getId())
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("부모 프로필이 존재하지 않습니다."));

        // 시터 프로필을 조회합니다.
        SitterProfile sitterProfile = sitterProfileRepository.findById(request.sitterProfileId())
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필을 찾을 수 없습니다."));

        // 시작/종료 시각을 검증합니다.
        if (!request.endAt().isAfter(request.startAt())) {
            // 종료가 시작보다 빠르거나 같으면 예외를 던집니다.
            throw new IllegalArgumentException("종료 시각은 시작 시각 이후여야 합니다.");
        }
        // 과거 시각 예약을 방지합니다.
        if (!request.startAt().isAfter(LocalDateTime.now())) {
            // 시작 시간이 현재 이전/동일이면 예외를 던집니다.
            throw new IllegalArgumentException("시작 시각은 현재 시각 이후여야 합니다.");
        }
        // 최소 예약 시간을 검증합니다.
        long durationMinutes = Duration.between(request.startAt(), request.endAt()).toMinutes();
        if (durationMinutes < minimumDurationMinutes) {
            // 정책보다 짧은 예약이면 예외를 던집니다.
            throw new IllegalArgumentException("최소 예약 시간은 " + minimumDurationMinutes + "분입니다.");
        }

        // REQUESTED 겹침 정책에 따라 검사할 상태 목록을 구성합니다.
        Set<ReservationStatus> overlapCheckStatuses = allowRequestedOverlap
                // REQUESTED 동시 요청을 허용하면 ACCEPTED만 검사합니다.
                ? Set.of(ReservationStatus.ACCEPTED)
                // REQUESTED 동시 요청을 허용하지 않으면 REQUESTED도 함께 검사합니다.
                : Set.of(ReservationStatus.ACCEPTED, ReservationStatus.REQUESTED);

        String lockWindowKey = request.startAt() + "_" + request.endAt();
        // 동일 시터/시간대 요청은 분산 락으로 직렬화합니다.
        return reservationLockService.executeWithSitterTimeslotLock(
                sitterProfile.getId(),
                lockWindowKey,
                () -> {
                    // 정책 상태 목록 기준으로 시간이 겹치면 신청을 막습니다.
                    boolean hasOverlapAccepted = reservationRepository.existsBySitterProfileIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                            sitterProfile.getId(),
                            // 정책에 따라 겹침 검사 상태를 사용합니다.
                            overlapCheckStatuses,
                            // 겹침 조건에서 endAt 비교에 사용할 요청 종료 시각입니다.
                            request.endAt(),
                            // 겹침 조건에서 startAt 비교에 사용할 요청 시작 시각입니다.
                            request.startAt()
                    );
                    // 겹치는 예약이 있으면 예외를 던집니다.
                    if (hasOverlapAccepted) {
                        // 이미 다른 예약이 확정된 시간대면 신청을 막습니다.
                        throw new IllegalArgumentException("해당 시간에는 이미 확정된 예약이 존재합니다.");
                    }

                    // 예약 엔티티를 생성합니다.
                    Reservation reservation = new Reservation(
                            parentProfile,
                            sitterProfile,
                            request.startAt(),
                            request.endAt(),
                            request.note()
                    );
                    // 예약을 저장합니다.
                    Reservation saved = reservationRepository.save(reservation);
                    Long sitterUserId = saved.getSitterProfile().getUser().getId();
                    Long newReservationId = saved.getId();
                    runAfterCommit(() -> {
                        reservationNotificationService.notifyReservationEvent(sitterUserId, "REQUESTED", newReservationId);
                        asyncEventPublisher.publishReservationStatusChanged("RESERVATION_REQUESTED", saved);
                    });
                    // 저장 결과를 응답으로 변환합니다.
                    return toResponse(saved);
                }
        );
    }

    // 시터가 예약을 수락합니다.
    @Transactional
    public ReservationResponse acceptReservation(Long reservationId) {
        // 현재 로그인한 사용자 이메일을 가져옵니다.
        String email = SecurityUtil.getCurrentUserEmail();
        // 사용자 정보를 조회합니다.
        User user = userRepository.findByEmailIgnoreCase(email)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 시터만 수락할 수 있습니다.
        if (user.getRole() != UserRole.SITTER) {
            // 역할이 다르면 예외를 던집니다.
            throw new IllegalArgumentException("시터 계정만 예약을 수락할 수 있습니다.");
        }
        // 시터 프로필을 조회합니다.
        SitterProfile sitterProfile = sitterProfileRepository.findByUserId(user.getId())
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필이 존재하지 않습니다."));

        // 예약을 조회합니다.
        Reservation reservation = reservationRepository.findById(reservationId)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 내 예약인지 확인합니다.
        if (!reservation.getSitterProfile().getId().equals(sitterProfile.getId())) {
            // 내 예약이 아니면 예외를 던집니다.
            throw new IllegalArgumentException("본인에게 들어온 예약만 수락할 수 있습니다.");
        }

        String lockWindowKey = reservation.getStartAt() + "_" + reservation.getEndAt();
        // 동일 시터/시간대 수락 시도는 분산 락으로 직렬화합니다.
        return reservationLockService.executeWithSitterTimeslotLock(
                sitterProfile.getId(),
                lockWindowKey,
                () -> {
                    // 다른 ACCEPTED 예약과 시간이 겹치면 수락을 막습니다.
                    boolean hasOverlapAccepted = reservationRepository.existsBySitterProfileIdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                            sitterProfile.getId(),
                            // 완료된 예약만 기준으로 겹침을 검사합니다.
                            Set.of(ReservationStatus.ACCEPTED),
                            // 겹침 조건에서 endAt 비교에 사용할 요청 종료 시각입니다.
                            reservation.getEndAt(),
                            // 겹침 조건에서 startAt 비교에 사용할 요청 시작 시각입니다.
                            reservation.getStartAt(),
                            // 자기 자신 예약은 제외합니다.
                            reservation.getId()
                    );
                    // 겹치는 예약이 있으면 예외를 던집니다.
                    if (hasOverlapAccepted) {
                        // 이미 확정된 예약이 있는 시간대면 수락을 막습니다.
                        throw new IllegalArgumentException("해당 시간에는 이미 확정된 예약이 존재하여 수락할 수 없습니다.");
                    }

                    // 예약을 수락합니다.
                    reservation.accept();
                    // 변경된 예약을 저장합니다.
                    Reservation saved = reservationRepository.save(reservation);
                    Long parentUserId = saved.getParentProfile().getUser().getId();
                    Long acceptedId = saved.getId();
                    runAfterCommit(() -> {
                        reservationNotificationService.notifyReservationEvent(parentUserId, "ACCEPTED", acceptedId);
                        asyncEventPublisher.publishReservationStatusChanged("RESERVATION_ACCEPTED", saved);
                    });
                    // 응답으로 변환해 반환합니다.
                    return toResponse(saved);
                }
        );
    }

    // 시터가 예약을 거절합니다.
    @Transactional
    public ReservationResponse rejectReservation(Long reservationId) {
        // 현재 로그인한 사용자 이메일을 가져옵니다.
        String email = SecurityUtil.getCurrentUserEmail();
        // 사용자 정보를 조회합니다.
        User user = userRepository.findByEmailIgnoreCase(email)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 시터만 거절할 수 있습니다.
        if (user.getRole() != UserRole.SITTER) {
            // 역할이 다르면 예외를 던집니다.
            throw new IllegalArgumentException("시터 계정만 예약을 거절할 수 있습니다.");
        }
        // 시터 프로필을 조회합니다.
        SitterProfile sitterProfile = sitterProfileRepository.findByUserId(user.getId())
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("시터 프로필이 존재하지 않습니다."));

        // 예약을 조회합니다.
        Reservation reservation = reservationRepository.findById(reservationId)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 내 예약인지 확인합니다.
        if (!reservation.getSitterProfile().getId().equals(sitterProfile.getId())) {
            // 내 예약이 아니면 예외를 던집니다.
            throw new IllegalArgumentException("본인에게 들어온 예약만 거절할 수 있습니다.");
        }

        // 예약을 거절합니다.
        reservation.reject();
        // 변경된 예약을 저장합니다.
        Reservation saved = reservationRepository.save(reservation);
        Long parentUserId = saved.getParentProfile().getUser().getId();
        Long rejectedId = saved.getId();
        runAfterCommit(() -> {
            reservationNotificationService.notifyReservationEvent(parentUserId, "REJECTED", rejectedId);
            asyncEventPublisher.publishReservationStatusChanged("RESERVATION_REJECTED", saved);
        });
        // 응답으로 변환해 반환합니다.
        return toResponse(saved);
    }

    // 부모가 예약을 취소합니다.
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        // 현재 로그인한 사용자 이메일을 가져옵니다.
        String email = SecurityUtil.getCurrentUserEmail();
        // 사용자 정보를 조회합니다.
        User user = userRepository.findByEmailIgnoreCase(email)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 부모만 취소할 수 있습니다.
        if (user.getRole() != UserRole.PARENT) {
            // 역할이 다르면 예외를 던집니다.
            throw new IllegalArgumentException("부모 계정만 예약을 취소할 수 있습니다.");
        }
        // 부모 프로필을 조회합니다.
        ParentProfile parentProfile = parentProfileRepository.findByUserId(user.getId())
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("부모 프로필이 존재하지 않습니다."));

        // 예약을 조회합니다.
        Reservation reservation = reservationRepository.findById(reservationId)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 내 예약인지 확인합니다.
        if (!reservation.getParentProfile().getId().equals(parentProfile.getId())) {
            // 내 예약이 아니면 예외를 던집니다.
            throw new IllegalArgumentException("본인이 신청한 예약만 취소할 수 있습니다.");
        }

        // 예약을 취소합니다.
        reservation.cancel();
        // 변경된 예약을 저장합니다.
        Reservation saved = reservationRepository.save(reservation);
        Long sitterUserId = saved.getSitterProfile().getUser().getId();
        Long cancelledId = saved.getId();
        runAfterCommit(() -> {
            reservationNotificationService.notifyReservationEvent(sitterUserId, "CANCELLED", cancelledId);
            asyncEventPublisher.publishReservationStatusChanged("RESERVATION_CANCELLED", saved);
        });
        // 응답으로 변환해 반환합니다.
        return toResponse(saved);
    }

    // 내 예약 목록을 조회합니다. (부모는 신청 목록, 시터는 받은 요청 목록)
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations() {
        // 현재 로그인한 사용자 이메일을 가져옵니다.
        String email = SecurityUtil.getCurrentUserEmail();
        // 사용자 정보를 조회합니다.
        User user = userRepository.findByEmailIgnoreCase(email)
                // 없으면 예외를 던집니다.
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 부모면 부모 프로필 기반으로 예약을 조회합니다.
        if (user.getRole() == UserRole.PARENT) {
            // 부모 프로필을 조회합니다.
            ParentProfile parentProfile = parentProfileRepository.findByUserId(user.getId())
                    // 없으면 예외를 던집니다.
                    .orElseThrow(() -> new IllegalArgumentException("부모 프로필이 존재하지 않습니다."));
            // 예약 목록을 조회합니다.
            return reservationRepository.findByParentProfileIdOrderByCreatedAtDesc(parentProfile.getId())
                    // DTO로 변환합니다.
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        // 시터면 시터 프로필 기반으로 예약을 조회합니다.
        if (user.getRole() == UserRole.SITTER) {
            // 시터 프로필을 조회합니다.
            SitterProfile sitterProfile = sitterProfileRepository.findByUserId(user.getId())
                    // 없으면 예외를 던집니다.
                    .orElseThrow(() -> new IllegalArgumentException("시터 프로필이 존재하지 않습니다."));
            // 예약 목록을 조회합니다.
            return reservationRepository.findBySitterProfileIdOrderByCreatedAtDesc(sitterProfile.getId())
                    // DTO로 변환합니다.
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        // 역할이 알 수 없으면 빈 리스트를 반환합니다.
        return List.of();
    }

    private void runAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    // 예약 엔티티를 응답 DTO로 변환합니다.
    private ReservationResponse toResponse(Reservation reservation) {
        boolean reviewed = reviewRepository.existsByReservationId(reservation.getId());
        // 응답 DTO를 생성해 반환합니다.
        return new ReservationResponse(
                reservation.getId(),
                reservation.getStatus().name(),
                reservation.getStartAt().toString(),
                reservation.getEndAt().toString(),
                reservation.getNote(),
                reservation.getParentProfile().getUser().getId(),
                reservation.getParentProfile().getUser().getName(),
                reservation.getSitterProfile().getId(),
                reservation.getSitterProfile().getUser().getId(),
                reservation.getSitterProfile().getUser().getName(),
                reviewed
        );
    }
}

