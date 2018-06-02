package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StrongLoginResponseIn {

    private AuthenticationToken authenticationToken;

    private Map<String, Object> errorMessage;

    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(AuthenticationToken authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public Map<String, Object> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Map<String, Object> errorMessage) {
        this.errorMessage = errorMessage;
    }
}
