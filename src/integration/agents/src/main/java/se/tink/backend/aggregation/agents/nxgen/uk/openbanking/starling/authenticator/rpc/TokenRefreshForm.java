package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenRefreshForm extends AbstractForm {

    private TokenRefreshForm(String refreshToken, String clientId, String clientSecret, String grantType) {
        put(StarlingConstants.RequestKey.REFRESH_TOKEN, refreshToken);
        put(StarlingConstants.RequestKey.CLIENT_ID, clientId);
        put(StarlingConstants.RequestKey.CLIENT_SECRET, clientSecret);
        put(StarlingConstants.RequestKey.GRANT_TYPE, grantType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final String GRANT_TYPE = "refresh_token";

        private String refreshToken;
        private String clientId;
        private String clientSecret;

        public Builder withRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder asClient(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenRefreshForm build() {
            return new TokenRefreshForm(refreshToken, clientId, clientSecret, GRANT_TYPE);
        }
    }
}
