package com.meli.streaming.dto;

public record StartStreamingRequest(
        String userId,
        String contentId,
        String region,
        String quality
) {}
