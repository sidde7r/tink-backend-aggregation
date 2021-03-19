package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {

    public TokenRequest(
            String code,
            String grantType,
            String redirectUri,
            String clientId,
            String clientSecret,
            String scope) {
        put("code", code);
        put("grant_type", grantType);
        put("redirect_uri", redirectUri);
        put("client_id", clientId);
        put("client_secret", clientSecret);
        put("scope", scope);
    }

    public static TokenRequestBuilder builder() {
        return new TokenRequestBuilder();
    }

    public static class TokenRequestBuilder {
        private String code;
        private String grantType;
        private String redirectUri;
        private String clientId;
        private String clientSecret;
        private String scope;

        public TokenRequestBuilder code(String code) {
            this.code = code;
            return this;
        }

        public TokenRequestBuilder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public TokenRequestBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenRequestBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenRequestBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(code, grantType, redirectUri, clientId, clientSecret, scope);
        }
    }
}
