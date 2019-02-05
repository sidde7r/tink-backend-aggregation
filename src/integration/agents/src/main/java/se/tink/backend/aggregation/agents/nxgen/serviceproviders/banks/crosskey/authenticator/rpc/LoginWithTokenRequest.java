package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

public class LoginWithTokenRequest {

    private String deviceId;
    private String deviceToken;
    private String password;

    private String appVersion;

    public String getDeviceId() {
        return deviceId;
    }

    public LoginWithTokenRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public LoginWithTokenRequest setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginWithTokenRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public LoginWithTokenRequest setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        return this;
    }
}
