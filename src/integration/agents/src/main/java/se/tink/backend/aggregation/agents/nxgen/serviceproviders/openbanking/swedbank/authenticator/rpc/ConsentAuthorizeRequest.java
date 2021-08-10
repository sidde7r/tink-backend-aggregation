package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAuthorizeRequest {
    private String authenticationMethodId;

    public ConsentAuthorizeRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
