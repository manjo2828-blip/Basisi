package com.basisi.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 로그인 사용자별 SSE 연결을 보관하고 예약 관련 이벤트를 전송합니다.
@Service
public class ReservationNotificationService {

    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        CopyOnWriteArrayList<SseEmitter> list =
                emittersByUserId.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        Runnable cleanup = () -> removeEmitter(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().comment("ok"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emittersByUserId.get(userId);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            emittersByUserId.remove(userId, list);
        }
    }

    /**
     * 특정 사용자에게 예약 변경 알림을 보냅니다. (해당 사용자의 모든 탭/연결)
     */
    public void notifyReservationEvent(Long userId, String action, Long reservationId) {
        if (userId == null || action == null || reservationId == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> list = emittersByUserId.get(userId);
        if (list == null || list.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "action", action,
                "reservationId", reservationId
        );

        for (SseEmitter emitter : List.copyOf(list)) {
            try {
                emitter.send(SseEmitter.event()
                        .name("reservation")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                removeEmitter(userId, emitter);
            }
        }
    }
}
