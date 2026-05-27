package com.meli.streaming.service;

import com.meli.streaming.client.CatalogServiceClient;
import com.meli.streaming.client.NotificationServiceClient;
import com.meli.streaming.client.SubscriptionServiceClient;
import com.meli.streaming.dto.StartStreamingRequest;
import com.meli.streaming.dto.StopStreamingRequest;
import com.meli.streaming.dto.StreamingSessionResponse;
import com.meli.streaming.entity.StreamingSession;
import com.meli.streaming.enums.ContentQuality;
import com.meli.streaming.enums.StreamingStatus;
import com.meli.streaming.enums.SubscriptionPlan;
import com.meli.streaming.exception.ContentNotAvailableException;
import com.meli.streaming.exception.InvalidStateTransitionException;
import com.meli.streaming.exception.SubscriptionInactiveException;
import com.meli.streaming.repository.StreamingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamingServiceTest {

    @Mock
    private StreamingSessionRepository sessionRepository;

    @Mock
    private SubscriptionServiceClient subscriptionClient;

    @Mock
    private CatalogServiceClient catalogClient;

    @Mock
    private NotificationServiceClient notificationClient;

    @InjectMocks
    private StreamingService streamingService;

    @Test
    void startStreaming_happyPath_returnsInitiatedSession() {
        String userId = "user123";
        String contentId = "movie_001";
        String region = "AR";

        when(subscriptionClient.checkSubscription(userId))
                .thenReturn(new SubscriptionServiceClient.SubscriptionInfo(SubscriptionPlan.STANDARD, true, 2));
        when(sessionRepository.findByUserIdAndStatusIn(eq(userId), any()))
                .thenReturn(List.of());
        when(catalogClient.checkContent(contentId, region))
                .thenReturn(new CatalogServiceClient.ContentInfo(true, List.of(ContentQuality.SD, ContentQuality.HD)));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StartStreamingRequest request = new StartStreamingRequest(userId, contentId, region, "HD");

        StreamingSessionResponse response = streamingService.startStreaming(request);

        assertNotNull(response);
        assertEquals(StreamingStatus.INITIATED, response.status());
        assertEquals(userId, response.userId());
        assertEquals(contentId, response.contentId());
        assertEquals("HD", response.quality());
        assertNotNull(response.streamingSessionId());
    }

    @Test
    void startStreaming_expiredSubscription_throwsSubscriptionInactiveException() {
        String userId = "expired_user1";

        when(subscriptionClient.checkSubscription(userId))
                .thenReturn(new SubscriptionServiceClient.SubscriptionInfo(SubscriptionPlan.STANDARD, false, 2));

        StartStreamingRequest request = new StartStreamingRequest(userId, "movie_001", "AR", "HD");

        assertThrows(SubscriptionInactiveException.class, () -> streamingService.startStreaming(request));
    }

    @Test
    void startStreaming_contentNotAvailableInRegion_throwsContentNotAvailableException() {
        String userId = "user123";
        String contentId = "content_BR_001";
        String region = "AR";

        when(subscriptionClient.checkSubscription(userId))
                .thenReturn(new SubscriptionServiceClient.SubscriptionInfo(SubscriptionPlan.STANDARD, true, 2));
        when(sessionRepository.findByUserIdAndStatusIn(eq(userId), any()))
                .thenReturn(List.of());
        when(catalogClient.checkContent(contentId, region))
                .thenReturn(new CatalogServiceClient.ContentInfo(false, List.of(ContentQuality.SD, ContentQuality.HD)));

        StartStreamingRequest request = new StartStreamingRequest(userId, contentId, region, "HD");

        assertThrows(ContentNotAvailableException.class, () -> streamingService.startStreaming(request));
    }

    @Test
    void pauseStreaming_sessionNotInProgress_throwsInvalidStateTransitionException() {
        StreamingSession session = new StreamingSession();
        session.setId("session-123");
        session.setUserId("user123");
        session.setStatus(StreamingStatus.PAUSED);
        session.setStartedAt(LocalDateTime.now());

        when(sessionRepository.findById("session-123")).thenReturn(Optional.of(session));

        assertThrows(InvalidStateTransitionException.class,
                () -> streamingService.pauseStreaming("session-123"));
    }

    @Test
    void stopStreaming_networkError_returnsFailedSession() {
        StreamingSession session = new StreamingSession();
        session.setId("session-456");
        session.setUserId("user123");
        session.setContentId("movie_001");
        session.setStatus(StreamingStatus.IN_PROGRESS);
        session.setQuality("HD");
        session.setStartedAt(LocalDateTime.now());

        when(sessionRepository.findById("session-456")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StreamingSessionResponse response = streamingService.stopStreaming("session-456",
                new StopStreamingRequest("NETWORK_ERROR"));

        assertEquals(StreamingStatus.FAILED, response.status());
    }
}
