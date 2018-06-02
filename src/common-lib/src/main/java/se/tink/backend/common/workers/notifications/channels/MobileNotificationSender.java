package se.tink.backend.common.workers.notifications.channels;

import io.dropwizard.lifecycle.Managed;
import java.util.List;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public interface MobileNotificationSender extends Managed {
    void sendNotifications(User user, List<Notification> notifications, boolean shouldBeTracked,
            boolean encrypted);
}
