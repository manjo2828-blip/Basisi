package com.basisi.backend.api.reservation;

// 예약 DTO import입니다.
import com.basisi.backend.api.reservation.dto.ReservationCreateRequest;
import com.basisi.backend.api.reservation.dto.ReservationResponse;
// 예약 서비스 import입니다.
import com.basisi.backend.service.ReservationService;
// 요청값 검증 어노테이션입니다.
import jakarta.validation.Valid;
// 리스트 타입 import입니다.
import java.util.List;
// HTTP 응답 객체 import입니다.
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// REST 매핑 어노테이션들입니다.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// 예약 관련 API를 제공하는 컨트롤러입니다.
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    // 예약 비즈니스 로직 서비스입니다.
    private final ReservationService reservationService;

    // 생성자로 서비스를 주입받습니다.
    public ReservationController(ReservationService reservationService) {
        // 주입받은 서비스를 필드에 저장합니다.
        this.reservationService = reservationService;
    }

    // 부모가 예약을 신청합니다.
    @PostMapping
    public ResponseEntity<ReservationResponse> request(@Valid @RequestBody ReservationCreateRequest request) {
        // 예약 신청을 처리합니다.
        ReservationResponse response = reservationService.requestReservation(request);
        // 201 Created로 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 예약 목록을 조회합니다. (부모: 신청 목록, 시터: 받은 요청 목록)
    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        // 내 예약 목록을 조회합니다.
        List<ReservationResponse> response = reservationService.getMyReservations();
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    // 시터가 예약을 수락합니다. (POST: 일부 환경에서 PATCH+빈 본문 시 인증/CORS 이슈로 403이 나는 경우가 있어 기본으로 POST 허용, PATCH는 호환용)
    @RequestMapping(value = "/{reservationId}/accept", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<ReservationResponse> accept(@PathVariable Long reservationId) {
        // 예약 수락을 처리합니다.
        ReservationResponse response = reservationService.acceptReservation(reservationId);
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    // 시터가 예약을 거절합니다.
    @RequestMapping(value = "/{reservationId}/reject", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<ReservationResponse> reject(@PathVariable Long reservationId) {
        // 예약 거절을 처리합니다.
        ReservationResponse response = reservationService.rejectReservation(reservationId);
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }

    // 부모가 예약을 취소합니다.
    @RequestMapping(value = "/{reservationId}/cancel", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long reservationId) {
        // 예약 취소를 처리합니다.
        ReservationResponse response = reservationService.cancelReservation(reservationId);
        // 200 OK로 응답을 반환합니다.
        return ResponseEntity.ok(response);
    }
}

