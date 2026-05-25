package com.basisi.backend.domain.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByReservationId(Long reservationId);

    List<Review> findBySitterProfileIdOrderByCreatedAtDesc(Long sitterProfileId);

    /** 집계 한 행을 List로 받아 Object[] 길이/래핑 이슈(H2 등)를 피합니다. */
    @Query("""
            select coalesce(avg(r.rating), 0), count(r)
            from Review r
            where r.sitterProfileId = :sitterProfileId
            """)
    List<Object[]> findSummaryBySitterProfileId(Long sitterProfileId);
}

