package com.basisi.backend.async.consumer;

import com.basisi.backend.domain.async.ProcessedEvent;
import com.basisi.backend.domain.async.ProcessedEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 이벤트 중복 소비를 방지하는 서비스입니다.
@Service
public class EventIdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public EventIdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public boolean tryMarkProcessed(String eventId, String consumerName) {
        try {
            processedEventRepository.save(new ProcessedEvent(eventId, consumerName));
            return true;
        } catch (DataIntegrityViolationException e) {
            // 유니크 키 충돌은 이미 처리된 이벤트입니다.
            return false;
        }
    }
}
