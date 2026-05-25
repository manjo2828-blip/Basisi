package com.basisi.backend.async.kafka;

import com.basisi.backend.async.AsyncEventPublisher;
import com.basisi.backend.async.event.ReservationStatusChangedEvent;
import com.basisi.backend.async.event.ReviewCreatedEvent;
import com.basisi.backend.domain.reservation.Reservation;
import com.basisi.backend.domain.review.Review;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Kafka로 도메인 이벤트를 발행합니다.
@Service
@ConditionalOnProperty(name = "async.events.enabled", havingValue = "true")
public class KafkaAsyncEventPublisher implements AsyncEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaEventProperties kafkaEventProperties;
    private final ObjectMapper objectMapper;

    public KafkaAsyncEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaEventProperties kafkaEventProperties,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEventProperties = kafkaEventProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishReservationStatusChanged(String eventType, Reservation reservation) {
        ReservationStatusChangedEvent event = new ReservationStatusChangedEvent(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                eventType,
                reservation.getId(),
                reservation.getParentProfile().getUser().getId(),
                reservation.getSitterProfile().getUser().getId(),
                reservation.getStatus().name(),
                reservation.getStartAt(),
                reservation.getEndAt()
        );
        publish(kafkaEventProperties.getTopics().getReservation(), String.valueOf(reservation.getId()), event);
    }

    @Override
    public void publishReviewCreated(Review review) {
        ReviewCreatedEvent event = new ReviewCreatedEvent(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "REVIEW_CREATED",
                review.getId(),
                review.getReservation().getId(),
                review.getSitterProfileId(),
                review.getReservation().getParentProfile().getUser().getId(),
                review.getRating()
        );
        publish(kafkaEventProperties.getTopics().getReview(), String.valueOf(review.getId()), event);
    }

    private void publish(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 직렬화에 실패했습니다.", e);
        }
    }
}
