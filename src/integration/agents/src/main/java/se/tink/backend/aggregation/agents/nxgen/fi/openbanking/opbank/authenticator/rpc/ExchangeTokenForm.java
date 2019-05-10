package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class ExchangeTokenForm extends AbstractForm {
    public ExchangeTokenForm() {
        this.put("grant_type", "authorization_code");
        this.put("scope", "accounts");
    }

    public ExchangeTokenForm setCode(String code) {
        this.put("code", code);
        return this;
    }

    public ExchangeTokenForm setClientSecret(String clientSecret) {
        this.put("client_secret", clientSecret);
        return this;
    }

    public ExchangeTokenForm setRedirectUri(String uri) {
        this.put("redirect_uri", uri);
        return this;
    }

    public ExchangeTokenForm setClientId(String clientId) {
        this.put("client_id", clientId);
        return this;
    }
}
