package com.basisi.backend.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** 로컬 단일 JVM용(예: h2 프로필). Redis 없이 ReentrantLock으로 동일 키 직렬화. */
@Service
@ConditionalOnProperty(prefix = "reservation.lock", name = "enabled", havingValue = "false")
public class InMemoryReservationLockService implements ReservationLockService {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final long waitSeconds;

    public InMemoryReservationLockService(@Value("${reservation.lock.wait-seconds:3}") long waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    @Override
    public <T> T executeWithSitterTimeslotLock(Long sitterProfileId, String lockWindowKey, Supplier<T> supplier) {
        String key = "reservation:lock:sitter:" + sitterProfileId + ":window:" + lockWindowKey;
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock(true));
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new IllegalArgumentException("예약 요청이 몰려 잠시 처리 지연 중입니다. 잠시 후 다시 시도해 주세요.");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("예약 처리 중 인터럽트가 발생했습니다. 다시 시도해 주세요.");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
