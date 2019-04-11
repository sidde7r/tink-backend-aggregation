package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities.CredentialsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest extends SpankkiRequest {
    private CredentialsEntity credentials;

    public LoginRequest() {}

    @JsonIgnore
    public static LoginRequest createUsernamePasswordLoginRequest(
            String username, String password) {
        LoginRequest request = new LoginRequest();
        return request.setCredentials(
                new CredentialsEntity().setUsername(username).setPassword(password));
    }

    @JsonIgnore
    public static LoginRequest createPinLoginRequest(String pin) {
        LoginRequest request = new LoginRequest();

        return request.setCredentials(new CredentialsEntity().setPin(pin));
    }

    @JsonIgnore
    public static LoginRequest createDeviceTokenLoginRequest(
            String password, String deviceId, String deviceToken) {
        LoginRequest request = new LoginRequest();
        request.setCredentials(new CredentialsEntity().setPassword(password).setToken(deviceToken));
        request.setDeviceId(deviceId);

        return request;
    }

    public LoginRequest setCredentials(CredentialsEntity credentials) {
        this.credentials = credentials;
        return this;
    }
}
