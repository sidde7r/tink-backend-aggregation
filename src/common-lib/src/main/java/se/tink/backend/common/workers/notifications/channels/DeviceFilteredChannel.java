package se.tink.backend.common.workers.notifications.channels;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class DeviceFilteredChannel implements MobileNotificationChannel {

    private final Predicate<Device> predicate;
    private final MobileNotificationChannel delegate;

    public DeviceFilteredChannel(MobileNotificationChannel delegate, Predicate<Device> predicate) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.predicate = Preconditions.checkNotNull(predicate);
    }

    @Override
    public boolean send(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {
        return delegate.send(notification, devices, user, encrypted, unreadNotifications);
    }

    @Override
    public Predicate<Device> getPredicate() {
        return Predicates.and(predicate, delegate.getPredicate());
    }

}
