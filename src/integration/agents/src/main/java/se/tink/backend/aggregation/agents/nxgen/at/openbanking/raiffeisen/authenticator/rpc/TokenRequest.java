package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(String grantType, String clientId, String scope) {
        put(RaiffeisenConstants.FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CLIENT_ID, clientId);
        put(RaiffeisenConstants.FormKeys.SCOPE, scope);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String scope;

        private String clientId;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this.grantType, this.clientId, this.scope);
        }
    }
}
