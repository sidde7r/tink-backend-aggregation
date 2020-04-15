package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class RefreshTokenForm extends AbstractForm {

    private RefreshTokenForm(
            String clientId, String grantType, String clientSecret, String refreshToken) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CLIENT_SECRET, clientSecret);
        put(FormKeys.REFRESH_TOKEN, refreshToken);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String clientId;
        private String grantType;
        private String clientSecret;
        private String refreshToken;

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public RefreshTokenForm build() {
            return new RefreshTokenForm(
                    this.clientId, this.grantType, this.clientSecret, this.refreshToken);
        }
    }
}
