package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(String redirect_uri, String grant_type, String code, String client_id) {
        put(BnpParibasBaseConstants.QueryKeys.CLIENT_ID, client_id);
        put(BnpParibasBaseConstants.QueryKeys.REDIRECT_URI, redirect_uri);
        put(BnpParibasBaseConstants.QueryKeys.GRANT_TYPE, grant_type);
        put(BnpParibasBaseConstants.QueryKeys.CODE, code);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String redirectUri;
        private String clientId;
        private String code;

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this.redirectUri, this.grantType, this.code, this.clientId);
        }
    }
}
