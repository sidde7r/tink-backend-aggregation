package se.tink.backend.aggregation.agents.utils.authentication.encap2.models;

public class DeviceRegistrationResponse {
    private final String userId;
    private final String deviceToken;

    public DeviceRegistrationResponse(String userId, String deviceToken) {
        this.userId = userId;
        this.deviceToken = deviceToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }
}
