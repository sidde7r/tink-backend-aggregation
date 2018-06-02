package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {
    private String username;
    private String password;
    @JsonProperty("device_token")
    private String deviceToken;
    @JsonProperty("device_id")
    private String deviceId;

    public LoginRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public LoginRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginRequest setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    public LoginRequest setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }
}
