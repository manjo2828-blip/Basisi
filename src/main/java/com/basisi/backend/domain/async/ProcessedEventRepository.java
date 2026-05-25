package com.basisi.backend.domain.async;

import org.springframework.data.jpa.repository.JpaRepository;

// 처리된 이벤트 이력 저장소입니다.
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
}
