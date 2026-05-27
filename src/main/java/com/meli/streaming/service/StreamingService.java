package com.meli.streaming.service;

import com.meli.streaming.client.CatalogServiceClient;
import com.meli.streaming.client.NotificationServiceClient;
import com.meli.streaming.client.SubscriptionServiceClient;
import com.meli.streaming.dto.StartStreamingRequest;
import com.meli.streaming.dto.StopStreamingRequest;
import com.meli.streaming.dto.StreamingSessionResponse;
import com.meli.streaming.entity.StreamingSession;
import com.meli.streaming.enums.StreamingStatus;
import com.meli.streaming.exception.ContentNotAvailableException;
import com.meli.streaming.exception.InvalidStateTransitionException;
import com.meli.streaming.exception.SessionNotFoundException;
import com.meli.streaming.exception.SubscriptionInactiveException;
import com.meli.streaming.repository.StreamingSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StreamingService {

    private final StreamingSessionRepository sessionRepository;
    private final SubscriptionServiceClient subscriptionClient;
    private final CatalogServiceClient catalogClient;
    private final NotificationServiceClient notificationClient;

    public StreamingService(StreamingSessionRepository sessionRepository,
                            SubscriptionServiceClient subscriptionClient,
                            CatalogServiceClient catalogClient,
                            NotificationServiceClient notificationClient) {
        this.sessionRepository = sessionRepository;
        this.subscriptionClient = subscriptionClient;
        this.catalogClient = catalogClient;
        this.notificationClient = notificationClient;
    }

    public StreamingSessionResponse startStreaming(StartStreamingRequest request) {
        System.out.println("Starting streaming for user " + request.userId());

        SubscriptionServiceClient.SubscriptionInfo subscription =
                subscriptionClient.checkSubscription(request.userId());

        if (!subscription.isActive()) {
            throw new SubscriptionInactiveException(
                    "User " + request.userId() + " does not have an active subscription");
        }

        List<StreamingSession> activeSessions = sessionRepository.findByUserIdAndStatusIn(
                request.userId(),
                List.of(StreamingStatus.INITIATED, StreamingStatus.IN_PROGRESS, StreamingStatus.PAUSED)
        );

        if (activeSessions.size() >= subscription.getMaxScreens()) {
            throw new SubscriptionInactiveException(
                    "Screen limit reached for plan " + subscription.getPlan().name());
        }

        CatalogServiceClient.ContentInfo content =
                catalogClient.checkContent(request.contentId(), request.region());

        if (!content.isAvailableInRegion()) {
            throw new ContentNotAvailableException(
                    "Content " + request.contentId() + " not available in region " + request.region());
        }

        boolean qualityAvailable = content.getAvailableQualities().stream()
                .anyMatch(q -> q.getValue().equals(request.quality()));

        if (!qualityAvailable) {
            throw new ContentNotAvailableException(
                    "Quality " + request.quality() + " not available for content " + request.contentId());
        }

        StreamingSession session = new StreamingSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(request.userId());
        session.setContentId(request.contentId());
        session.setRegion(request.region());
        session.setQuality(request.quality());
        session.setStatus(StreamingStatus.INITIATED);
        session.setStartedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        StreamingSession saved = sessionRepository.save(session);

        System.out.println("Streaming session created: " + saved.getId());

        notificationClient.notifyStreamingStarted(saved.getId(), request.userId(), request.contentId());

        return toResponse(saved);
    }

    public StreamingSessionResponse pauseStreaming(String sessionId) {
        StreamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() != StreamingStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException(
                    "Cannot pause session in status " + session.getStatus());
        }

        session.setStatus(StreamingStatus.PAUSED);
        session.setUpdatedAt(LocalDateTime.now());

        return toResponse(sessionRepository.save(session));
    }

    public StreamingSessionResponse resumeStreaming(String sessionId) {
        StreamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() != StreamingStatus.PAUSED) {
            throw new InvalidStateTransitionException(
                    "Cannot resume session in status " + session.getStatus());
        }

        session.setStatus(StreamingStatus.IN_PROGRESS);
        session.setUpdatedAt(LocalDateTime.now());

        return toResponse(sessionRepository.save(session));
    }

    public StreamingSessionResponse stopStreaming(String sessionId, StopStreamingRequest request) {
        StreamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        StreamingStatus newStatus = "NETWORK_ERROR".equals(request.reason())
                ? StreamingStatus.FAILED
                : StreamingStatus.COMPLETED;

        session.setStatus(newStatus);
        session.setUpdatedAt(LocalDateTime.now());
        session.setEndedAt(LocalDateTime.now());

        System.out.println("Streaming session stopped: " + sessionId + " reason " + request.reason());

        return toResponse(sessionRepository.save(session));
    }

    public StreamingSessionResponse getSession(String sessionId) {
        StreamingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
        return toResponse(session);
    }

    public List<StreamingSessionResponse> getUserSessions(String userId) {
        return sessionRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private StreamingSessionResponse toResponse(StreamingSession session) {
        return new StreamingSessionResponse(
                session.getId(),
                session.getUserId(),
                session.getContentId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getQuality()
        );
    }
}
