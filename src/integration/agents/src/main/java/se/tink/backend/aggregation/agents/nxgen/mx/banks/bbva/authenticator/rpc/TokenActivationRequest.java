package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenActivationRequest {
    private String deviceIdentifier;
    private String salt;
    private String authenticationCode;
    private String applicationCode;

    public TokenActivationRequest(String deviceIdentifier, String salt, String authenticationCode) {
        this.applicationCode = "RETAILMX";
        this.deviceIdentifier = deviceIdentifier;
        this.salt = salt;
        this.authenticationCode = authenticationCode;
    }

    public String getApplicationCode() {
        return applicationCode;
    }
}
