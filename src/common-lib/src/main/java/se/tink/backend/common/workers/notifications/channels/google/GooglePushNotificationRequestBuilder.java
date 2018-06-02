package se.tink.backend.common.workers.notifications.channels.google;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.common.utils.NotificationUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.common.workers.notifications.channels.encryption.EncryptedPushNotificationContainer;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.utils.LogUtils;

public class GooglePushNotificationRequestBuilder {

    private static final LogUtils log = new LogUtils(GooglePushNotificationRequestBuilder.class);

    private boolean encryption;
    private List<Device> devices;
    private Notification notification;
    private String fallback_url = "tink://open";

    public GooglePushNotificationRequestBuilder withEncryption(boolean encryption) {
        this.encryption = encryption;
        return this;
    }

    public GooglePushNotificationRequestBuilder withNotification(Notification notification) {
        this.notification = notification;
        return this;
    }

    public GooglePushNotificationRequestBuilder withDevices(List<Device> devices) {
        this.devices = devices;
        return this;
    }

    public GooglePushNotificationRequestBuilder withFallbackUrl(String fallbackUrl) {
        this.fallback_url = fallbackUrl;
        return this;
    }

    /**
     * Return a list of google push notification request based on the input
     *
     * - One request is returned if encryption is disabled (since google support sending the exact same notification to
     * several devices)
     *
     * - One request for every device is returned if encryption is enabled
     */
    public List<GooglePushNotificationRequest> build() {

        Preconditions.checkNotNull(notification);
        Preconditions.checkNotNull(devices);

        // Verify that the notification and the devices matches
        Preconditions.checkState(FluentIterable.from(devices).allMatch(
                device -> Objects.equal(notification.getUserId(), device.getUserId())));

        if (encryption) {
            return createEncryptedNotificationRequests();
        } else {
            return Lists.newArrayList(createPlainTextNotificationRequest());
        }
    }

    private List<GooglePushNotificationRequest> createEncryptedNotificationRequests() {

        List<GooglePushNotificationRequest> requests = Lists.newArrayList();

        for (Device device : devices) {
            try {
                EncryptedPushNotificationContainer encryptedNotification = NotificationUtils
                        .getEncryptedPushNotification(notification, device);

                GooglePushNotificationRequest request = new GooglePushNotificationRequest();
                request.setEncryptedContent(SerializationUtils.serializeToString(encryptedNotification));
                request.setPushTokens(Lists.newArrayList(device.getNotificationToken()));
                request.setDevices(ImmutableList.of(device));

                if (encryptedNotification == null) {
                    log.error(device.getUserId(), "Could not create encrypted notification");
                } else {
                    requests.add(request);
                }

            } catch (Exception e) {
                log.error(device.getUserId(), "Could not create encrypted notification.", e);
            }
        }

        return requests;
    }

    private GooglePushNotificationRequest createPlainTextNotificationRequest() {

        GooglePushNotificationRequest request = new GooglePushNotificationRequest();
        request.setMessage(notification.getMessage());
        request.setType(notification.getType());
        request.setTitle(notification.getTitle());
        request.setKey(notification.getKey());
        request.setUrl(getNotificationUrl(notification));
        request.setPushTokens(FluentIterable.from(devices).transform(Device::getNotificationToken).toList());
        request.setDevices(devices);

        return request;
    }

    private String getNotificationUrl(Notification notification) {
        if (!Strings.isNullOrEmpty(notification.getUrl())) {
            return notification.getUrl();
        } else {
            log.warn(String.format(
                    "Notification of type '%s' has a missing URL. Setting fallback URL. Please patch backend.",
                    notification.getType()));
            return fallback_url;
        }
    }
}
