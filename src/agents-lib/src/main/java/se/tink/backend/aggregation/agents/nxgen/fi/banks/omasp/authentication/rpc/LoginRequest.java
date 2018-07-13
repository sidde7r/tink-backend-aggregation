package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    private String userId;
    private String password;
    private String deviceId;

    public LoginRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public LoginRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }
}
