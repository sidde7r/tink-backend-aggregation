package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenForm extends AbstractForm {
    public TokenForm(String scope) {
        this.put(
                OpBankConstants.RefreshTokenFormKeys.GRANT_TYPE,
                OpBankConstants.RefreshTokenFormValues.CLIENT_CREDENTIALS);
        this.put(OpBankConstants.AuthorizationKeys.SCOPE, scope);
    }

    public TokenForm setClientId(String clientId) {
        this.put(OpBankConstants.RefreshTokenFormKeys.CLIENT_ID, clientId);
        return this;
    }

    public TokenForm setClientSecret(String clientSecret) {
        this.put(OpBankConstants.RefreshTokenFormKeys.CLIENT_SECRET, clientSecret);
        return this;
    }
}
