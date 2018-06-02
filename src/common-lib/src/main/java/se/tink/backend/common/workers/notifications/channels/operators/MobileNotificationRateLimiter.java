package se.tink.backend.common.workers.notifications.channels.operators;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class MobileNotificationRateLimiter implements MobileNotificationOperator {
    private static final LogUtils log = new LogUtils(MobileNotificationRateLimiter.class);
    private static final int MAX_CACHE_SIZE = 20000; // We generally send out ~11000 notifications after 9AM.
    private static final int MAX_RATE_PERIOD_IN_SECONDS = 30;
    private static final double MAX_PERMITS_PER_SECOND = 1.0 / MAX_RATE_PERIOD_IN_SECONDS; // Once every 30 seconds

    private final LoadingCache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder()
            .maximumSize(MAX_CACHE_SIZE).expireAfterAccess(MAX_RATE_PERIOD_IN_SECONDS + 1, TimeUnit.SECONDS)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String cacheKey) throws Exception {
                    return RateLimiter.create(MAX_PERMITS_PER_SECOND);
                }

            });
    private final MobileNotificationOperator nextOperator;

    public MobileNotificationRateLimiter(MobileNotificationOperator nextOperator) {
        this.nextOperator = Preconditions.checkNotNull(nextOperator);
    }

    @Override
    public void process(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {
        boolean success = false;
        try {
            final String cacheKey = String.format("%s.%s", user.getId(), notification.getKey());
            final RateLimiter rateLimiter = rateLimiters.get(cacheKey);

            if (!rateLimiter.tryAcquire()) {
                log.warn(user.getId(),
                        String.format("Resending notification is blocked by rate limiter. Notification: %s",
                                cacheKey));
            } else {
                success = true;
            }
        } catch (ExecutionException e) {
            log.warn("Could instantiate rateLimiter.", e);
        }

        if (success) {
            nextOperator.process(notification, devices, user, encrypted, unreadNotifications);
        }
    }
}
