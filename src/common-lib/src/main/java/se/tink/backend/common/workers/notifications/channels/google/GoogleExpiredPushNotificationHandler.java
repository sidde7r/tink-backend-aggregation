package se.tink.backend.common.workers.notifications.channels.google;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.core.Device;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class GoogleExpiredPushNotificationHandler {

    private static final String GOOGLE_DEVICE_NOT_REGISTERED_ERROR = "NotRegistered";
    private static final LogUtils log = new LogUtils(GoogleMobileNotificationChannel.class);
    private final DeviceRepository deviceRepository;

    private static final MetricId DEVICES_REMOVED = MetricId.newId("mobile_devices_removed").label("device", "google");
    private final MetricRegistry metricRegistry;

    public GoogleExpiredPushNotificationHandler(DeviceRepository repository, MetricRegistry metricRegistry) {
        this.deviceRepository = repository;
        this.metricRegistry = metricRegistry;
    }

    public void handle(List<GooglePushNotificationResponseResult> results, List<Device> devices, User user,
            boolean encrypted) {
        List<Device> devicesToRemove = Lists.newArrayList();
        List<Device> devicesToUpdate = Lists.newArrayList();

        if (results.size() != devices.size()) {
            log.warn(user.getId(), String.format(
                    "Number of results does not match number of devices (Results = '%d', Devices = '%d')",
                    results.size(), devices.size()));
            return;
        }

        for (int i = 0; i < results.size(); i++) {
            // Response result is ordered in the same way as the input
            Device device = devices.get(i);

            final GooglePushNotificationResponseResult result = results.get(i);

            if (result.hasError()) {
                final String message = String
                        .format("Could not send Android notification on device %s. Error was: %s", device.toString(),
                                result.getError());

                if (Objects.equal(GOOGLE_DEVICE_NOT_REGISTERED_ERROR, result.getError())) {
                    metricRegistry.meter(DEVICES_REMOVED.label("encrypted", encrypted)).inc();
                    devicesToRemove.add(device);
                    log.info(user.getId(), message);
                } else {
                    log.error(user.getId(), message);
                }
            } else if (result.isInvalidToken()) {
                boolean isExistingDevice = Iterables.any(devices,
                        d -> d.getNotificationToken().equals(result.getToken()));

                // Remove this device since we already have the correct one
                if (isExistingDevice) {
                    log.info(user.getId(), "Removing expired android device: " + device.toString());
                    devicesToRemove.add(device);

                    // Update the current user device with correct device
                } else {
                    log.info(user.getId(), "Replacing expired android device: " + device.toString());
                    device.setNotificationToken(result.getToken());
                    devicesToUpdate.add(device);
                }
            } else {
                log.info(user.getId(), "Push notification sent for device: " + device.toString());
            }
        }

        if (!devicesToRemove.isEmpty()) {
            deviceRepository.delete(devicesToRemove);
        }

        if (!devicesToUpdate.isEmpty()) {
            deviceRepository.save(devicesToUpdate);
        }
    }
}
