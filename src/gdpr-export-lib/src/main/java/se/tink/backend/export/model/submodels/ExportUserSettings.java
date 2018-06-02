package se.tink.backend.export.model.submodels;

public class ExportUserSettings {

    private final ExportNotificationSettings NotificationSettings;

    public ExportUserSettings(ExportNotificationSettings notificationSettings) {
        NotificationSettings = notificationSettings;
    }

    public ExportNotificationSettings getNotificationSettings() {
        return NotificationSettings;
    }
}
