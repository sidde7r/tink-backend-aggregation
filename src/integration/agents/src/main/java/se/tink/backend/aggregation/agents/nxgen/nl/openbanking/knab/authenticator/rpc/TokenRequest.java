package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class TokenRequest {
    private String grantType;
    private String code;
    private String clientId;
    private String clientSecret;
    private String state;
    private String redirectUri;
    private String refreshToken;

    private TokenRequest(Builder builder) {
        grantType = builder.grantType;
        code = builder.code;
        clientId = builder.clientId;
        clientSecret = builder.clientSecret;
        state = builder.state;
        redirectUri = builder.redirectUri;
        refreshToken = builder.refreshToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String grantType;
        private String code;
        private String clientId;
        private String clientSecret;
        private String state;
        private String redirectUri;
        private String refreshToken;

        public Builder grantType(String val) {
            grantType = val;
            return this;
        }

        public Builder code(String val) {
            code = val;
            return this;
        }

        public Builder clientId(String val) {
            clientId = val;
            return this;
        }

        public Builder clientSecret(String val) {
            clientSecret = val;
            return this;
        }

        public Builder state(String val) {
            state = val;
            return this;
        }

        public Builder redirectUri(String val) {
            redirectUri = val;
            return this;
        }

        public Builder refreshToken(String val) {
            refreshToken = val;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(this);
        }
    }

    public String toTokenData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CODE, code)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .put(FormKeys.STATE, state)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }

    public String toRefreshTokenData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .put(FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
