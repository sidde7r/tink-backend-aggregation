package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenForm extends AbstractForm {
    private TokenForm(
            String grantType,
            String code,
            String redirectUri,
            Boolean validRequest,
            String codeVerifier,
            String clientId,
            String refreshToken) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        Optional.ofNullable(code).ifPresent(v -> put(FormKeys.CODE, v));
        Optional.ofNullable(redirectUri).ifPresent(v -> put(FormKeys.REDIRECT_URI, v));
        Optional.ofNullable(validRequest)
                .ifPresent(v -> put(FormKeys.VALID_REQUEST, String.valueOf(v)));
        Optional.ofNullable(codeVerifier).ifPresent(v -> put(FormKeys.CODE_VERIFIER, v));
        Optional.ofNullable(refreshToken).ifPresent(v -> put(FormKeys.REFRESH_TOKEN, v));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String code;
        private String redirectUri;
        private String codeVerifier;
        private Boolean validRequest;
        private String clientId;
        private String refreshToken;

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

        public Builder setValidRequest(Boolean validRequest) {
            this.validRequest = validRequest;
            return this;
        }

        public Builder setCodeVerifier(String codeVerifier) {
            this.codeVerifier = codeVerifier;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenForm build() {
            return new TokenForm(
                    this.grantType,
                    this.code,
                    this.redirectUri,
                    this.validRequest,
                    this.codeVerifier,
                    this.clientId,
                    this.refreshToken);
        }
    }
}
