package com.meli.streaming.dto;

import com.meli.streaming.enums.StreamingStatus;

import java.time.LocalDateTime;

public record StreamingSessionResponse(
        String streamingSessionId,
        String userId,
        String contentId,
        StreamingStatus status,
        LocalDateTime startedAt,
        String quality
) {}
