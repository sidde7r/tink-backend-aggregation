package se.tink.backend.aggregation.agents.utils.authentication.encap2.models;

public class DeviceAuthenticationResponse {
    private final String userId;
    private final String deviceToken;
    private final String hardwareId;

    public DeviceAuthenticationResponse(String userId, String deviceToken, String hardwareId) {
        this.userId = userId;
        this.deviceToken = deviceToken;
        this.hardwareId = hardwareId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getHardwareId() {
        return hardwareId;
    }
}
