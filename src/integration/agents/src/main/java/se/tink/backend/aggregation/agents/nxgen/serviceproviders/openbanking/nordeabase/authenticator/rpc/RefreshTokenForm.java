package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class RefreshTokenForm extends AbstractForm {
    private RefreshTokenForm(String grantType, String redirectUri, String refreshToken) {
        put(NordeaBaseConstants.FormKeys.GRANT_TYPE, grantType);
        put(NordeaBaseConstants.FormKeys.REDIRECT_URI, redirectUri);
        put(NordeaBaseConstants.FormKeys.REFRESH_TOKEN, refreshToken);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String redirectUri;
        private String refreshToken;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public RefreshTokenForm build() {
            return new RefreshTokenForm(this.grantType, this.redirectUri, this.refreshToken);
        }
    }
}
