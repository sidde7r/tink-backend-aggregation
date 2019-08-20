package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthGrantTypeEntity;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AccessTokenRequest extends AbstractForm {
    public AccessTokenRequest withGrantType(AuthGrantTypeEntity grantType) {
        this.put("grant_type", grantType.toString());
        return this;
    }

    public AccessTokenRequest withPendingCode(String pendingCode) {
        this.put("pending_code", pendingCode);
        return this;
    }

    public AccessTokenRequest withRedirectUrl(String redirectUrl) {
        this.put("redirect_uri", redirectUrl);
        return this;
    }
}
