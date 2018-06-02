package se.tink.backend.common.workers.notifications.channels.operators;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.List;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.CounterCacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class MobileNotificationSuccessfulInstrumenter implements MobileNotificationOperator {
    // TODO: Is this metric counting the same as the SuccessNotificationHandler / GoogleMobileNotificationChannel?
    // I don't trust that it is not, so the metric is different here.
    private static final MetricId METER_PREFIX = MetricId.newId("mobile_notifications_instrumented")
            .label("service", "apple");
    private final LoadingCache<MetricId.MetricLabels, Counter> notificationMeters;

    public MobileNotificationSuccessfulInstrumenter(MetricRegistry metricRegistry) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        notificationMeters = cacheBuilder.build(new CounterCacheLoader(metricRegistry, METER_PREFIX));
    }

    @Override
    public void process(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {
        notificationMeters.getUnchecked(new MetricId.MetricLabels().add("type", notification.getType())).inc();
    }
}
