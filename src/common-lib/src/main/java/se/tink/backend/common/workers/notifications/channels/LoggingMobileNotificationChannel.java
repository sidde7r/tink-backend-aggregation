package se.tink.backend.common.workers.notifications.channels;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class LoggingMobileNotificationChannel implements MobileNotificationChannel {

    private static final LogUtils log = new LogUtils(LoggingMobileNotificationChannel.class);

    @Override
    public boolean send(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {
        log.warn(user.getId(), String.format("Not sending push notification %s to device %s for user %s.",
                notification, devices, user));
        return false;
    }

    @Override
    public Predicate<Device> getPredicate() {
        return Predicates.alwaysTrue();
    }

}
