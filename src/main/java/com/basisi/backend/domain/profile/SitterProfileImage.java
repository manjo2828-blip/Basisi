package com.basisi.backend.domain.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시터 프로필에 올라간 이미지(바이너리)입니다. 공개 GET은 불투명 id로만 접근합니다. */
@Entity
@Table(name = "sitter_profile_images")
@Getter
@NoArgsConstructor
public class SitterProfileImage {

    @Id
    @Column(length = 40)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // H2(PostgreSQL MODE) 및 실제 PostgreSQL 모두에서 동작하도록 BYTEA로 명시합니다.
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] data;

    @Column(name = "content_type", length = 80)
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SitterProfileImage(String id, Long userId, byte[] data, String contentType) {
        this.id = id;
        this.userId = userId;
        this.data = data;
        this.contentType = contentType;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
