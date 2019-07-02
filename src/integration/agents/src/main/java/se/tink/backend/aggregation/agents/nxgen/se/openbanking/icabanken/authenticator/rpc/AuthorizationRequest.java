package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AuthorizationRequest extends AbstractForm {
    private AuthorizationRequest(
            String client_id, String redirect_uri, String grant_type, String code) {
        put(IcaBankenConstants.QueryKeys.CLIENT_ID, client_id);
        put(IcaBankenConstants.QueryKeys.REDIRECT_URI, redirect_uri);
        put(IcaBankenConstants.QueryKeys.GRANT_TYPE, grant_type);
        put(IcaBankenConstants.QueryKeys.CODE, code);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String client_id;
        private String redirect_uri;
        private String grant_type;
        private String code;

        public Builder setClientId(String clientId) {
            this.client_id = clientId;
            return this;
        }

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

        public AuthorizationRequest build() {
            return new AuthorizationRequest(
                    this.client_id, this.redirect_uri, this.grant_type, this.code);
        }
    }
}
