package se.tink.backend.core.notifications;

import com.google.common.collect.Lists;
import java.util.List;

public class NotificationGroup {
    private String title;
    private List<NotificationType> types;

    public NotificationGroup(String title) {
        this.title = title;
        this.types = Lists.newArrayList();
    }

    public String getTitle() {
        return title;
    }

    public List<NotificationType> getTypes() {
        return types;
    }

    public void addNotificationType(NotificationType type) {
        types.add(type);
    }
}
