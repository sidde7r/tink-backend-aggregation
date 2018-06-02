package se.tink.backend.common.workers.notifications.channels.apple;

import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.utils.LogUtils;

public class SuccessNotificationHandler implements NotificationResponseHandler {

    private static final LogUtils log = new LogUtils(SuccessNotificationHandler.class);

    private static final MetricId NOTIFICATIONS_SUCCESS = MetricId.newId("mobile_notifications")
            .label("service", "apple").label("success", true);
    private MetricRegistry metricRegistry;

    public SuccessNotificationHandler(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void handle(Notification notification, Device device, boolean encrypted,
            PushNotificationResponse<SimpleApnsPushNotification> response) {

        metricRegistry.meter(NOTIFICATIONS_SUCCESS.label("encrypted", encrypted)).inc();

        log.info(device.getUserId(),
                String.format("Push notification sent (Key = '%s', Device = '%s')", notification.getKey(), device));
    }
}
