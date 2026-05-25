package com.basisi.backend.async.consumer;

import com.basisi.backend.async.event.ReviewCreatedEvent;
import com.basisi.backend.service.SitterScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// 리뷰 생성 이벤트를 소비해 후속 비동기 처리를 수행합니다.
@Component
@ConditionalOnProperty(name = "async.events.enabled", havingValue = "true")
public class ReviewEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReviewEventConsumer.class);
    private static final String CONSUMER_NAME = "review-event-consumer";

    private final ObjectMapper objectMapper;
    private final EventIdempotencyService eventIdempotencyService;
    private final SitterScoreService sitterScoreService;

    public ReviewEventConsumer(
            ObjectMapper objectMapper,
            EventIdempotencyService eventIdempotencyService,
            SitterScoreService sitterScoreService
    ) {
        this.objectMapper = objectMapper;
        this.eventIdempotencyService = eventIdempotencyService;
        this.sitterScoreService = sitterScoreService;
    }

    @KafkaListener(
            topics = "${async.events.kafka.topics.review:basisi.review-events}",
            groupId = "${async.events.kafka.consumer-group:basisi-event-consumers}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String payload) throws Exception {
        ReviewCreatedEvent event = objectMapper.readValue(payload, ReviewCreatedEvent.class);
        if (!eventIdempotencyService.tryMarkProcessed(event.eventId(), CONSUMER_NAME)) {
            log.info("[Async] skip duplicated review event. eventId={}", event.eventId());
            return;
        }

        if (event.sitterProfileId() != null) {
            sitterScoreService.recalculateBySitterProfileId(event.sitterProfileId());
        }
        log.info("[Async] review event consumed. reviewId={}, rating={}", event.reviewId(), event.rating());
    }

    @KafkaListener(
            topics = "${async.events.kafka.topics.review:basisi.review-events}.DLT",
            groupId = "${async.events.kafka.consumer-group:basisi-event-consumers}-dlt"
    )
    public void consumeDlt(String payload) {
        log.error("[Async] review event moved to DLT. payload={}", payload);
    }
}
