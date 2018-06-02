package se.tink.backend.system.rpc;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class SendNotificationsRequest {
    private List<UserNotificationsContainer> userNotifications = Lists.newArrayList();

    public SendNotificationsRequest() {
    }

    public SendNotificationsRequest(User user, List<Notification> notifications, boolean encrypted) {
        addUserNotifications(user, notifications, encrypted);
    }

    public void addUserNotifications(User user, List<Notification> notifications, boolean encrypted) {
        userNotifications.add(new UserNotificationsContainer(user, notifications, encrypted));
    }

    public void addUserNotification(User user, Notification notification, boolean encrypted) {
        addUserNotifications(user, Collections.singletonList(notification), encrypted);
    }

    public List<UserNotificationsContainer> getUserNotifications() {
        return userNotifications;
    }

    public void setUserNotifications(List<UserNotificationsContainer> userNotifications) {
        this.userNotifications = userNotifications;
    }
}
