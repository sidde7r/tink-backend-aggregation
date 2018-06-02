package se.tink.backend.common.workers.notifications.channels;

import com.google.common.base.Predicate;
import java.util.List;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public interface MobileNotificationChannel {

    boolean send(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications);
    
    Predicate<Device> getPredicate();

}
