package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClaimEntity {
    private AuthorizationIdEntity authorizationId;
    private AcrEntity acr;

    public ClaimEntity(
        AuthorizationIdEntity authorizationId,
        AcrEntity acr) {
        this.authorizationId = authorizationId;
        this.acr = acr;
    }
}
