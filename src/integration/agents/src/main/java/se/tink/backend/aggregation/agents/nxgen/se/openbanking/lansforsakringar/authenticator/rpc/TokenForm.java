package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenForm extends AbstractForm {

    private TokenForm(
            String clientId,
            String grantType,
            String code,
            String clientSecret,
            String redirectUri) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CODE, code);
        put(FormKeys.CLIENT_SECRET, clientSecret);
        put(FormKeys.REDIRECT_URI, redirectUri);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientId;
        private String grantType;
        private String code;
        private String clientSecret;
        private String redirect_uri;

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

        public Builder setRedirectUri(String redirect_uri) {
            this.redirect_uri = redirect_uri;
            return this;
        }

        public TokenForm build() {
            return new TokenForm(
                    this.clientId, this.grantType, this.code, this.clientSecret, this.redirect_uri);
        }
    }
}
