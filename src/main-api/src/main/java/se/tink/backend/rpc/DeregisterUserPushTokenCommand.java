package se.tink.backend.rpc;

import java.util.Optional;

public class DeregisterUserPushTokenCommand {
    private Optional<String> notificationToken;
    private Optional<String> deviceId;

    public DeregisterUserPushTokenCommand() {
    }

    public DeregisterUserPushTokenCommand(Optional<String> notificationToken, Optional<String> deviceId) {
        this.notificationToken = notificationToken;
        this.deviceId = deviceId;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = Optional.ofNullable(notificationToken);
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = Optional.ofNullable(deviceId);
    }

    public Optional<String> getNotificationToken() {
        return notificationToken;
    }

    public Optional<String> getDeviceId() {
        return deviceId;
    }
}
