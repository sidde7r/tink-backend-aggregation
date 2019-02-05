package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.entities.AuthenticateResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticateResponse {
    @JsonProperty("AuthenticateResult")
    private AuthenticateResultEntity authenticateResult;

    public AuthenticateResultEntity getAuthenticateResult() {
        return authenticateResult;
    }
}
