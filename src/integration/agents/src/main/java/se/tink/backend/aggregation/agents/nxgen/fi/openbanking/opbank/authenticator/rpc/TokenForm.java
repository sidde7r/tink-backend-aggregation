package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenForm extends AbstractForm {
    public TokenForm() {
        this.put("grant_type", "client_credentials");
        this.put("scope", "accounts");
    }

    public TokenForm setClientId(String clientId) {
        this.put("client_id", clientId);
        return this;
    }

    public TokenForm setClientSecret(String clientSecret) {
        this.put("client_secret", clientSecret);
        return this;
    }
}
