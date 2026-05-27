package com.meli.streaming.client;

import com.meli.streaming.enums.SubscriptionPlan;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SubscriptionServiceClient {

    private final RestTemplate restTemplate;

    public SubscriptionServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SubscriptionInfo checkSubscription(String userId) {
        System.out.println("Checking subscription for user " + userId);
        try {
            if (userId.contains("slow_")) {
                Thread.sleep(15000);
            } else {
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (userId.startsWith("premium_")) {
            return new SubscriptionInfo(SubscriptionPlan.PREMIUM, true, 4);
        }
        if (userId.startsWith("basic_")) {
            return new SubscriptionInfo(SubscriptionPlan.BASIC, true, 1);
        }
        if (userId.startsWith("expired_")) {
            return new SubscriptionInfo(SubscriptionPlan.STANDARD, false, 2);
        }
        return new SubscriptionInfo(SubscriptionPlan.STANDARD, true, 2);
    }

    public static class SubscriptionInfo {
        private final SubscriptionPlan plan;
        private final boolean active;
        private final int maxScreens;

        public SubscriptionInfo(SubscriptionPlan plan, boolean active, int maxScreens) {
            this.plan = plan;
            this.active = active;
            this.maxScreens = maxScreens;
        }

        public SubscriptionPlan getPlan() { return plan; }
        public boolean isActive() { return active; }
        public int getMaxScreens() { return maxScreens; }
    }
}
