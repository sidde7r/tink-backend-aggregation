package se.tink.backend.core.notifications;

public class NotificationType {
    private String key;
    private String title;
    private boolean enabled;

    public NotificationType(String key, String title, boolean enabled) {
        this.key = key;
        this.title = title;
        this.enabled = enabled;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
