package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class RefreshTokenRequest extends AbstractForm {
    private RefreshTokenRequest(
            String client_id, String scope, String grant_type, String refresh_token) {
        put(IcaBankenConstants.QueryKeys.CLIENT_ID, client_id);
        put(IcaBankenConstants.QueryKeys.SCOPE, scope);
        put(IcaBankenConstants.QueryKeys.GRANT_TYPE, grant_type);
        put(IcaBankenConstants.QueryKeys.REFRESH_TOKEN, refresh_token);
    }

    public static RefreshTokenRequest.Builder builder() {
        return new RefreshTokenRequest.Builder();
    }

    public static class Builder {
        private String client_id;
        private String grant_type;
        private String refresh_token;
        private String scope;

        public Builder setClientId(String clientId) {
            this.client_id = clientId;
            return this;
        }

        public Builder setGrantType(String grantType) {
            this.grant_type = grantType;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refresh_token = refreshToken;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public RefreshTokenRequest build() {
            return new RefreshTokenRequest(
                    this.client_id, this.scope, this.grant_type, this.refresh_token);
        }
    }
}
