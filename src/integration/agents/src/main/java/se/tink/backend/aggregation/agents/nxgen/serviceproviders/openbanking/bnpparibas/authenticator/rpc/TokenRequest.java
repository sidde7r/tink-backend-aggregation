package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(String redirect_uri, String grant_type, String code) {
        //    put(BnpParibasConstants.QueryKeys.CLIENT_ID, client_id);
        put(BnpParibasBaseConstants.QueryKeys.REDIRECT_URI, redirect_uri);
        put(BnpParibasBaseConstants.QueryKeys.GRANT_TYPE, grant_type);
        put(BnpParibasBaseConstants.QueryKeys.CODE, code);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grant_type;
        private String redirect_uri;
        // private String client_id;
        private String code;

        /*    public Builder setClientId(String clientId) {
            this.client_id = clientId;
            return this;
        }*/

        public Builder setGrantType(String grantType) {
            this.grant_type = grantType;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirect_uri = redirectUri;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this.redirect_uri, this.grant_type, this.code);
        }
    }
}
