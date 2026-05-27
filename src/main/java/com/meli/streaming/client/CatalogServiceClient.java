package com.meli.streaming.client;

import com.meli.streaming.enums.ContentQuality;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class CatalogServiceClient {

    private final RestTemplate restTemplate;

    public CatalogServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ContentInfo checkContent(String contentId, String region) {
        System.out.println("Checking content " + contentId + " for region " + region);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean availableInRegion;
        if (contentId.startsWith("content_AR_")) {
            availableInRegion = "AR".equals(region);
        } else if (contentId.startsWith("content_BR_")) {
            availableInRegion = "BR".equals(region);
        } else {
            availableInRegion = true;
        }

        List<ContentQuality> qualities;
        if (contentId.startsWith("content_4k_")) {
            qualities = List.of(ContentQuality.SD, ContentQuality.HD, ContentQuality.FOUR_K);
        } else {
            qualities = List.of(ContentQuality.SD, ContentQuality.HD);
        }

        return new ContentInfo(availableInRegion, qualities);
    }

    public static class ContentInfo {
        private final boolean availableInRegion;
        private final List<ContentQuality> availableQualities;

        public ContentInfo(boolean availableInRegion, List<ContentQuality> availableQualities) {
            this.availableInRegion = availableInRegion;
            this.availableQualities = availableQualities;
        }

        public boolean isAvailableInRegion() { return availableInRegion; }
        public List<ContentQuality> getAvailableQualities() { return availableQualities; }
    }
}
