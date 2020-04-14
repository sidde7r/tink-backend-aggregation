package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeAgreementResponseEntity {
    private AuthenticationTokenEntity authenticationToken;

    public String getToken() {
        return authenticationToken.getToken();
    }
}
