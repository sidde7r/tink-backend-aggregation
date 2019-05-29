package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class LoginRequest {
    private String username;
    private String password;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_token")
    private String deviceToken;

    public LoginRequest setUsername(String username) {
        this.username = username;
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

    public LoginRequest setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }
}
