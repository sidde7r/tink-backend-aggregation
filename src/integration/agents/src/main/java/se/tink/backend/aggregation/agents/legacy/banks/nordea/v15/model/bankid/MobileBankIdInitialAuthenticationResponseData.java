package se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid;

import java.util.Map;

import se.tink.backend.aggregation.agents.banks.nordea.v15.model.AuthenticationToken;

import com.google.common.collect.Maps;

public class MobileBankIdInitialAuthenticationResponseData {
    private AuthenticationToken authenticationToken;
    private Map<String, Object> bankIdAuthenticationRequestToken = Maps.newHashMap();

    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    public Map<String, Object> getBankIdAuthenticationRequestToken() {
        return bankIdAuthenticationRequestToken;
    }
    
    public void setAuthenticationToken(AuthenticationToken authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public void setBankIdAuthenticationRequestToken(Map<String, Object> bankIdAuthenticationRequestToken) {
        this.bankIdAuthenticationRequestToken = bankIdAuthenticationRequestToken;
    }
}
