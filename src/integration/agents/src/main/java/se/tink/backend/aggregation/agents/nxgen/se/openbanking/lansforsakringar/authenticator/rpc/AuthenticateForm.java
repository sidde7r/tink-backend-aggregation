package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AuthenticateForm extends AbstractForm {

    private AuthenticateForm(
            String grantType,
            String clientId,
            String clientSecret,
            String code,
            String redirectUri) {
        put(LansforsakringarConstants.FormKeys.GRANT_TYPE, grantType);
        put(LansforsakringarConstants.FormKeys.CODE, code);
        put(LansforsakringarConstants.FormKeys.CLIENT_ID, clientId);
        put(LansforsakringarConstants.FormKeys.CLIENT_SECRET, clientSecret);
        put(LansforsakringarConstants.FormKeys.REDIRECT_URI, redirectUri);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String clientId;
        private String clientSecret;
        private String code;
        private String redirectUri;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public AuthenticateForm build() {
            return new AuthenticateForm(
                    this.grantType, this.clientId, this.clientSecret, this.code, this.redirectUri);
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }
    }
}
