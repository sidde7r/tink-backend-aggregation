package se.tink.backend.common.workers.notifications.channels.apple;

import com.google.common.collect.ImmutableSet;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.utils.LogUtils;

public class FailedNotificationHandler implements NotificationResponseHandler {

    private static final LogUtils log = new LogUtils(FailedNotificationHandler.class);

    private static final ImmutableSet<String> REMOVE_DEVICE_REASONS = ImmutableSet
            .of("Unregistered", "DeviceTokenNotForTopic");

    private static final MetricId NOTIFICATIONS_FAILED = MetricId.newId("mobile_notifications")
            .label("service", "apple").label("success", false);

    private static final MetricId DEVICES_REMOVED = MetricId.newId("mobile_devices_removed").label("device", "apple");

    private final DeviceRepository deviceRepository;
    private MetricRegistry metricRegistry;

    public FailedNotificationHandler(DeviceRepository deviceRepository, MetricRegistry metricRegistry) {

        this.deviceRepository = deviceRepository;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void handle(Notification notification, Device device, boolean encrypted,
            PushNotificationResponse<SimpleApnsPushNotification> response) {
        metricRegistry.meter(NOTIFICATIONS_FAILED.label("encrypted", encrypted)).inc();

        if (REMOVE_DEVICE_REASONS.contains(response.getRejectionReason())) {

            metricRegistry.meter(DEVICES_REMOVED.label("encrypted", encrypted)).inc();

            log.warn(device.getUserId(), String.format("Removing device (Device = '%s', Reason = '%s')",
                    device.toString(), response.getRejectionReason()));

            deviceRepository.delete(device);
        } else {
            log.error(device.getUserId(),
                    String.format("Notification rejected (Device = '%s', Reason = '%s')", device.toString(),
                            response.getRejectionReason()));
        }
    }
}
