package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateConsentRequest {
    private String authenticationMethodId;

    public UpdateConsentRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
