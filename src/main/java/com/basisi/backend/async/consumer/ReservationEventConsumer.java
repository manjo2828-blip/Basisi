package com.basisi.backend.async.consumer;

import com.basisi.backend.async.event.ReservationStatusChangedEvent;
import com.basisi.backend.service.SitterScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// 예약 이벤트를 소비해 후속 비동기 처리를 수행합니다.
@Component
@ConditionalOnProperty(name = "async.events.enabled", havingValue = "true")
public class ReservationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventConsumer.class);
    private static final String CONSUMER_NAME = "reservation-event-consumer";

    private final ObjectMapper objectMapper;
    private final EventIdempotencyService eventIdempotencyService;
    private final SitterScoreService sitterScoreService;

    public ReservationEventConsumer(
            ObjectMapper objectMapper,
            EventIdempotencyService eventIdempotencyService,
            SitterScoreService sitterScoreService
    ) {
        this.objectMapper = objectMapper;
        this.eventIdempotencyService = eventIdempotencyService;
        this.sitterScoreService = sitterScoreService;
    }

    @KafkaListener(
            topics = "${async.events.kafka.topics.reservation:basisi.reservation-events}",
            groupId = "${async.events.kafka.consumer-group:basisi-event-consumers}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String payload) throws Exception {
        ReservationStatusChangedEvent event = objectMapper.readValue(payload, ReservationStatusChangedEvent.class);
        if (!eventIdempotencyService.tryMarkProcessed(event.eventId(), CONSUMER_NAME)) {
            log.info("[Async] skip duplicated reservation event. eventId={}", event.eventId());
            return;
        }

        // 예약 이벤트 수신 시 예약ID로 시터를 역추적해 불꽃 점수를 재계산합니다.
        if (event.reservationId() != null) {
            sitterScoreService.recalculateByReservationId(event.reservationId());
        }
        log.info("[Async] reservation event consumed. type={}, reservationId={}", event.eventType(), event.reservationId());
    }

    @KafkaListener(
            topics = "${async.events.kafka.topics.reservation:basisi.reservation-events}.DLT",
            groupId = "${async.events.kafka.consumer-group:basisi-event-consumers}-dlt"
    )
    public void consumeDlt(String payload) {
        log.error("[Async] reservation event moved to DLT. payload={}", payload);
    }
}
