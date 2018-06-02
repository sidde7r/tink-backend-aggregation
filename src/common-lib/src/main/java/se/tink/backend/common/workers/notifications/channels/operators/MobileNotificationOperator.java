package se.tink.backend.common.workers.notifications.channels.operators;

import java.util.List;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public interface MobileNotificationOperator {
    void process(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications);
}
