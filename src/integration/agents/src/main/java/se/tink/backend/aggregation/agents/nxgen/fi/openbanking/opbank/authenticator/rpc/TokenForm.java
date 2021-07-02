package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenForm extends AbstractForm {
    public TokenForm(String scope) {
        this.put("grant_type", "client_credentials");
        this.put("scope", scope);
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
