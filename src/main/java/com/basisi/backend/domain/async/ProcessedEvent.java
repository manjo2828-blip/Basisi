package com.basisi.backend.domain.async;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 소비자 중복 처리를 위한 처리 이력 엔티티입니다.
@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_processed_event_consumer",
                columnNames = {"event_id", "consumer_name"}
        )
)
@Getter
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 120)
    private String eventId;

    @Column(name = "consumer_name", nullable = false, length = 120)
    private String consumerName;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedEvent(String eventId, String consumerName) {
        this.eventId = eventId;
        this.consumerName = consumerName;
    }

    @PrePersist
    public void prePersist() {
        this.processedAt = LocalDateTime.now();
    }
}
