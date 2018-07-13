package se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid;

import java.util.Map;

import se.tink.backend.aggregation.agents.banks.nordea.v15.model.AuthenticationToken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileBankIdAuthenticationResultResponseData {
    private AuthenticationToken authenticationToken;
    private Map<String, Object> progressStatus = Maps.newHashMap();

    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    public Map<String, Object> getProgressStatus() {
        return progressStatus;
    }

    public void setAuthenticationToken(AuthenticationToken authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public void setProgressStatus(Map<String, Object> progressStatus) {
        this.progressStatus = progressStatus;
    }
}
