package com.meli.streaming.entity;

import com.meli.streaming.enums.StreamingStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "streaming_sessions")
public class StreamingSession {

    @Id
    private String id;

    private String userId;
    private String contentId;
    private String region;
    private String quality;

    @Enumerated(EnumType.STRING)
    private StreamingStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public StreamingStatus getStatus() { return status; }
    public void setStatus(StreamingStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
}
