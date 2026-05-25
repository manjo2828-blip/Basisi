package com.basisi.backend.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "reservation.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedissonReservationLockService implements ReservationLockService {

    private final RedissonClient redissonClient;
    private final long waitSeconds;
    private final long leaseSeconds;

    public RedissonReservationLockService(
            RedissonClient redissonClient,
            @Value("${reservation.lock.wait-seconds:3}") long waitSeconds,
            @Value("${reservation.lock.lease-seconds:10}") long leaseSeconds
    ) {
        this.redissonClient = redissonClient;
        this.waitSeconds = waitSeconds;
        this.leaseSeconds = leaseSeconds;
    }

    @Override
    public <T> T executeWithSitterTimeslotLock(Long sitterProfileId, String lockWindowKey, Supplier<T> supplier) {
        String key = "reservation:lock:sitter:" + sitterProfileId + ":window:" + lockWindowKey;
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
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
