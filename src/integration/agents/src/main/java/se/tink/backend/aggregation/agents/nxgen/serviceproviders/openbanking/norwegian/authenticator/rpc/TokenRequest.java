package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(
            String redirectUri,
            String grantType,
            String code,
            String clientId,
            String codeVerifier,
            String clientSecret) {
        put(NorwegianConstants.QueryKeys.CLIENT_ID, clientId);
        put(NorwegianConstants.QueryKeys.REDIRECT_URI, redirectUri);
        put(NorwegianConstants.QueryKeys.GRANT_TYPE, grantType);
        put(NorwegianConstants.QueryKeys.CODE, code);
        put(NorwegianConstants.QueryKeys.CODE_VERIFIER, codeVerifier);
        put(NorwegianConstants.QueryKeys.CLIENT_SECRET, clientSecret);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String redirectUri;
        private String clientId;
        private String code;
        private String codeVerifier;
        private String clientSecret;

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

        public Builder setCodeVerifier(String codeVerifier) {
            this.codeVerifier = codeVerifier;
            return this;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(
                    this.redirectUri,
                    this.grantType,
                    this.code,
                    this.clientId,
                    this.codeVerifier,
                    this.clientSecret);
        }
    }
}
