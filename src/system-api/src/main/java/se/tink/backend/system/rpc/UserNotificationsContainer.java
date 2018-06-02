package se.tink.backend.system.rpc;

import java.util.List;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class UserNotificationsContainer {
    private User user;
    private List<Notification> notifications;
    private boolean encrypted;

    public UserNotificationsContainer () {

    }

    public UserNotificationsContainer(User user, List<Notification> notifications, boolean encrypted) {
        this.user = user;
        this.notifications = notifications;
        this.encrypted = encrypted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
}
