package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GetTokenForm extends AbstractForm {
    private GetTokenForm(
            String grantType,
            String clientId,
            String clientSecret,
            String code,
            String redirectUri) {
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.CLIENT_SECRET, clientSecret);
        put(FormKeys.CODE, code);
        put(FormKeys.REDIRECT_URI, redirectUri);
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

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
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

        public GetTokenForm build() {
            return new GetTokenForm(
                    this.grantType, this.clientId, this.clientSecret, this.code, this.redirectUri);
        }
    }
}
