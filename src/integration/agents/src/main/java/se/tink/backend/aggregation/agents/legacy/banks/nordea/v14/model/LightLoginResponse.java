package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LightLoginResponse {
    private AuthenticationToken authenticationToken;
    private ErrorMessage errorMessage;

    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setAuthenticationToken(AuthenticationToken authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }
}
