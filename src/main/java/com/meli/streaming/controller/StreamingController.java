package com.meli.streaming.controller;

import com.meli.streaming.dto.StartStreamingRequest;
import com.meli.streaming.dto.StopStreamingRequest;
import com.meli.streaming.dto.StreamingSessionResponse;
import com.meli.streaming.service.StreamingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/streamings")
public class StreamingController {

    private final StreamingService streamingService;

    public StreamingController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @PostMapping
    public ResponseEntity<StreamingSessionResponse> startStreaming(@RequestBody StartStreamingRequest request) {
        StreamingSessionResponse response = streamingService.startStreaming(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{sessionId}/pause")
    public ResponseEntity<StreamingSessionResponse> pauseStreaming(@PathVariable String sessionId) {
        return ResponseEntity.ok(streamingService.pauseStreaming(sessionId));
    }

    @PutMapping("/{sessionId}/resume")
    public ResponseEntity<StreamingSessionResponse> resumeStreaming(@PathVariable String sessionId) {
        return ResponseEntity.ok(streamingService.resumeStreaming(sessionId));
    }

    @PutMapping("/{sessionId}/stop")
    public ResponseEntity<StreamingSessionResponse> stopStreaming(@PathVariable String sessionId,
                                                                   @RequestBody StopStreamingRequest request) {
        return ResponseEntity.ok(streamingService.stopStreaming(sessionId, request));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<StreamingSessionResponse> getSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(streamingService.getSession(sessionId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StreamingSessionResponse>> getUserSessions(@PathVariable String userId) {
        return ResponseEntity.ok(streamingService.getUserSessions(userId));
    }
}
