package com.basisi.backend.async.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Kafka 이벤트 파이프라인 설정 바인딩 클래스입니다.
@ConfigurationProperties(prefix = "async.events.kafka")
public class KafkaEventProperties {

    private String bootstrapServers = "localhost:9092";
    private String consumerGroup = "basisi-event-consumers";
    private final Topics topics = new Topics();

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Topics getTopics() {
        return topics;
    }

    public static class Topics {
        private String reservation = "basisi.reservation-events";
        private String review = "basisi.review-events";

        public String getReservation() {
            return reservation;
        }

        public void setReservation(String reservation) {
            this.reservation = reservation;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }
}
