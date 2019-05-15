package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(String grantType, String scope) {
        put(RaiffeisenConstants.FormKeys.GRANT_TYPE, grantType);
        put(RaiffeisenConstants.FormKeys.SCOPE, scope);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String scope;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this.grantType, this.scope);
        }
    }
}
