package com.basisi.backend.service;

import java.util.function.Supplier;

/** 예약 시터·타임슬롯 단위 직렬화(분산 또는 인메모리). */
public interface ReservationLockService {

    <T> T executeWithSitterTimeslotLock(Long sitterProfileId, String lockWindowKey, Supplier<T> supplier);
}
