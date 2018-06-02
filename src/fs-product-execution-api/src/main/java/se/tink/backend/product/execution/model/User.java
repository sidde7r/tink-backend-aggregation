package se.tink.backend.product.execution.model;

import java.util.UUID;

public class User {
    private UUID userId;
    private String locale;
    private String deviceId;

    public User() {
    }

    public User(UUID userId, String locale, String deviceId) {
        this.userId = userId;
        this.locale = locale;
        this.deviceId = deviceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
