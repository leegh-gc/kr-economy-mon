package com.example.kreconomonmon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_cache")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnalysisCache {

    @Id
    @Column(name = "cache_key", length = 50)
    private String cacheKey;

    @Column(name = "cache_type", nullable = false, length = 30)
    private String cacheType;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "image_data", columnDefinition = "TEXT")
    private String imageData;

    @Column(name = "data_hash", length = 64)
    private String dataHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateContent(String contentText, String imageData, String dataHash) {
        this.contentText = contentText;
        this.imageData = imageData;
        this.dataHash = dataHash;
        this.updatedAt = LocalDateTime.now();
    }
}
