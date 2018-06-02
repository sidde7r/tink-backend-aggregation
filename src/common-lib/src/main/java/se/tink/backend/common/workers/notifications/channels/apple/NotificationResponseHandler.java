package se.tink.backend.common.workers.notifications.channels.apple;

import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;

public interface NotificationResponseHandler {
    void handle(Notification notification, Device device, boolean encrypted,
            PushNotificationResponse<SimpleApnsPushNotification> response);
}
