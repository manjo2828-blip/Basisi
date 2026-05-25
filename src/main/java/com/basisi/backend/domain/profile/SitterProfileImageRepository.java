package com.basisi.backend.domain.profile;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SitterProfileImageRepository extends JpaRepository<SitterProfileImage, String> {

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);

    List<SitterProfileImage> findByUserId(Long userId);
}
