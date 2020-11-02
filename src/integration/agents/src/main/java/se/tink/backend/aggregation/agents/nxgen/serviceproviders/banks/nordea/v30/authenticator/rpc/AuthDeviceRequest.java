package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthDeviceRequest {
    private String signature;

    @JsonProperty("code_challenge")
    private String codeChallenge;

    @JsonProperty("client_nonce")
    private String clientNonce;

    public static AuthDeviceRequest create(
            String signature, String codeChallenge, String clientNonce) {
        AuthDeviceRequest authDeviceRequest = new AuthDeviceRequest();
        authDeviceRequest.clientNonce = clientNonce;
        authDeviceRequest.codeChallenge = codeChallenge;
        authDeviceRequest.signature = signature;
        return authDeviceRequest;
    }
}
