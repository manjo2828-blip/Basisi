package com.basisi.backend.api.publicmedia;

import com.basisi.backend.domain.profile.SitterProfileImage;
import com.basisi.backend.domain.profile.SitterProfileImageRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** 시터 프로필 이미지 공개 조회(불투명 id)입니다. */
@RestController
@RequestMapping("/api/public")
public class PublicSitterImageController {

    private final SitterProfileImageRepository sitterProfileImageRepository;

    public PublicSitterImageController(SitterProfileImageRepository sitterProfileImageRepository) {
        this.sitterProfileImageRepository = sitterProfileImageRepository;
    }

    @GetMapping("/sitter-images/{id}")
    public ResponseEntity<byte[]> get(@PathVariable String id) {
        SitterProfileImage img = sitterProfileImageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        String ct = img.getContentType() != null ? img.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, ct)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(img.getData());
    }
}
