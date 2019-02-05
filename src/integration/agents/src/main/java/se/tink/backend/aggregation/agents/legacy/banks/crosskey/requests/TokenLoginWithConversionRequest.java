package se.tink.backend.aggregation.agents.banks.crosskey.requests;

public class TokenLoginWithConversionRequest {

    private String deviceId;
    private String deviceToken;
    private String password;
    private String appVersion;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getPassword() {
        return password;
    }

    public String getAppVersion() {
        return appVersion;
    }
}
