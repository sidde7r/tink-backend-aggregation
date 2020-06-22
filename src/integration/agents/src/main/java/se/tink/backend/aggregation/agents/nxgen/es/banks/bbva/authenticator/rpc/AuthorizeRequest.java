package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities.AuthorizationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequest {
    private AuthorizationEntity authorizationEntity;

    public AuthorizeRequest(String username, String otpCode, String multistepProcessId) {
        this.authorizationEntity = new AuthorizationEntity(username, otpCode, multistepProcessId);
    }
}
