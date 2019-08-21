package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClaimEntity {
    private AuthorizationIdEntity authorizationId;

    @JsonInclude(Include.NON_NULL)
    private AcrEntity acr;

    public ClaimEntity(AuthorizationIdEntity authorizationId, AcrEntity acr) {
        this.authorizationId = authorizationId;
        this.acr = acr;
    }
}
