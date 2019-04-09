package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    @JsonProperty("AuthenticateResult")
    private AuthenticateResult authenticateResult;

    public AuthenticateResult getAuthenticateResult() {
        return authenticateResult;
    }

    public void setAuthenticateResult(AuthenticateResult authenticateResult) {
        this.authenticateResult = authenticateResult;
    }
}
