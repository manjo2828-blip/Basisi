package com.basisi.backend.api.notification;

import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.security.SecurityUtil;
import com.basisi.backend.service.ReservationNotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 인증된 사용자가 예약 실시간 알림(SSE)을 구독합니다.
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ReservationNotificationService reservationNotificationService;
    private final UserRepository userRepository;

    public NotificationController(
            ReservationNotificationService reservationNotificationService,
            UserRepository userRepository
    ) {
        this.reservationNotificationService = reservationNotificationService;
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return reservationNotificationService.subscribe(user.getId());
    }
}
