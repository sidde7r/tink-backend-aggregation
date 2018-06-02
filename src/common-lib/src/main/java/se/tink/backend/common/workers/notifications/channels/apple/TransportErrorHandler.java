package se.tink.backend.common.workers.notifications.channels.apple;

import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * The main responsibility of this class is to use prometheus metrics to track transport
 * failures when sending and APN notification through SEB's proxy servers
 */

public class TransportErrorHandler {
    private static final MetricId NOTIFICATIONS_FAILED = MetricId.newId("mobile_notifications")
            .label("service", "apple").label("success", false);

    private MetricRegistry metricRegistry;
    public TransportErrorHandler(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void handle(Notification notification, Device device, boolean encrypted) {
        metricRegistry.meter(NOTIFICATIONS_FAILED.label("encrypted", encrypted)).inc();
    }
}
