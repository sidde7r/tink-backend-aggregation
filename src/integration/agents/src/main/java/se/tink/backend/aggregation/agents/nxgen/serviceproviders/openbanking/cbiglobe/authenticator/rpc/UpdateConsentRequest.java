package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateConsentRequest {
    private String authenticationMethodId;

    public UpdateConsentRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
