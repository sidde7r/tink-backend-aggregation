package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class ExchangeTokenForm extends AbstractForm {
    public ExchangeTokenForm() {
        this.put(
                OpBankConstants.RefreshTokenFormKeys.GRANT_TYPE,
                OpBankConstants.RefreshTokenFormValues.AUTHORIZATION_CODE);
        this.put(OpBankConstants.AuthorizationKeys.SCOPE, OpBankConstants.TokenValues.ACCOUNTS);
    }

    public ExchangeTokenForm setCode(String code) {
        this.put(OpBankConstants.TokenValues.RESPONSE_TYPE, code);
        return this;
    }

    public ExchangeTokenForm setClientSecret(String clientSecret) {
        this.put(OpBankConstants.RefreshTokenFormKeys.CLIENT_SECRET, clientSecret);
        return this;
    }

    public ExchangeTokenForm setRedirectUri(String uri) {
        this.put(OpBankConstants.RefreshTokenFormKeys.REDIRECT_URI, uri);
        return this;
    }

    public ExchangeTokenForm setClientId(String clientId) {
        this.put(OpBankConstants.RefreshTokenFormKeys.CLIENT_ID, clientId);
        return this;
    }
}
