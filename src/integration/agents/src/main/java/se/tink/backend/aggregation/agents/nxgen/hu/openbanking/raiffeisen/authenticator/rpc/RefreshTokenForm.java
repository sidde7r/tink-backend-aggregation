package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class RefreshTokenForm extends AbstractForm {
    private RefreshTokenForm(
            String grantType, String clientId, String clientSecret, String refreshToken) {
        put(RaiffeisenConstants.FormKeys.GRANT_TYPE, grantType);
        put(RaiffeisenConstants.FormKeys.CLIENT_ID, clientId);
        put(RaiffeisenConstants.FormKeys.CLIENT_SECRET, clientSecret);
        put(RaiffeisenConstants.FormKeys.REFRESH_TOKEN, refreshToken);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String clientId;
        private String clientSecret;
        private String refreshToken;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
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

        public RefreshTokenForm build() {
            return new RefreshTokenForm(
                    this.grantType, this.clientId, this.clientSecret, this.refreshToken);
        }
    }
}
