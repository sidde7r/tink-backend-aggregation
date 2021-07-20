package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationResponse {
    // uri might be in one of two
    private String aspspRedirectUri;
    private String authorizationRedirectUri;

    public String getRedirectUri() {
        if (aspspRedirectUri != null) {
            return aspspRedirectUri;
        } else {
            return authorizationRedirectUri;
        }
    }
}
