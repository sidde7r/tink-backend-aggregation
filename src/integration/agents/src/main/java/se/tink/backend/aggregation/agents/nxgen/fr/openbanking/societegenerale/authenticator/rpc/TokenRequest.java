package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {

    private TokenRequest(String redirect_uri, String grant_type, String code, String scope) {
        put(SocieteGeneraleConstants.QueryKeys.REDIRECT_URI, redirect_uri);
        put(SocieteGeneraleConstants.QueryKeys.GRANT_TYPE, grant_type);
        if (code != null) {
            put(SocieteGeneraleConstants.QueryKeys.CODE, code);
        }
        if (scope != null) {
            put(SocieteGeneraleConstants.QueryKeys.SCOPE, scope);
        }
    }

    private TokenRequest(String grant_type, String refreshToken) {
        put(SocieteGeneraleConstants.QueryKeys.REFRESH_TOKEN, refreshToken);
        put(SocieteGeneraleConstants.QueryKeys.GRANT_TYPE, grant_type);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String redirect_uri;
        private String grant_type;
        private String code;
        private String refreshToken;
        private String scope;

        public Builder setGrantType(String grantType) {
            this.grant_type = grantType;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirect_uri = redirectUri;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this.redirect_uri, this.grant_type, this.code, this.scope);
        }

        public TokenRequest buildRefresh() {
            return new TokenRequest(this.grant_type, this.refreshToken);
        }
    }
}
