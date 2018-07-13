package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateAuthenticationRequest {
    private String token;
    private String deviceId;
    private String authenticationMethod;

    @JsonObject
    public static InitiateAuthenticationRequest create(Sparebank1Identity identity) {
        InitiateAuthenticationRequest request = new InitiateAuthenticationRequest();

        request.setToken(identity.getToken());
        request.setDeviceId(identity.getDeviceId());
        request.setAuthenticationMethod("pin");

        return request;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
}
