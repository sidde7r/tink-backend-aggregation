package se.tink.libraries.rate_limit_service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RateLimitService {
    INSTANCE;
    private final Logger LOG = LoggerFactory.getLogger(RateLimitService.class);
    private final ConcurrentHashMap<String, LocalDateTime> rateLimitNotifications =
            new ConcurrentHashMap<>();
    private final int RATE_LIMIT_MINUTES = 5;

    public boolean hasReceivedRateLimitNotificationRecently(String providerName) {
        if (providerName == null) {
            return false;
        }
        LocalDateTime lastRateLimitNotification = rateLimitNotifications.get(providerName);
        if (lastRateLimitNotification != null
                && lastRateLimitNotification.isAfter(
                        LocalDateTime.now().minusMinutes(RATE_LIMIT_MINUTES))) {
            LOG.warn(
                    "Provider {} was rate limited at {}",
                    providerName,
                    lastRateLimitNotification.toString());
            return true;
        }
        return false;
    }

    public void notifyRateLimitExceeded(String providerName) {
        if (providerName != null) {
            LOG.warn("Received notification that provider {} was rate limited", providerName);
            rateLimitNotifications.put(providerName, LocalDateTime.now());
        }
    }
}
