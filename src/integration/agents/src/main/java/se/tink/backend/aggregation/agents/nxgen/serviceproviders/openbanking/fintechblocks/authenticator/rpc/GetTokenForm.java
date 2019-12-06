package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GetTokenForm extends AbstractForm {
    private GetTokenForm(
            String grantType,
            String scope,
            String clientAssertionType,
            String clientAssertion,
            String redirectUri,
            String code,
            String refreshToken) {
        put(FormKeys.GRANT_TYPE, grantType);
        Optional.ofNullable(clientAssertionType)
                .ifPresent(v -> put(FormKeys.CLIENT_ASSERTION_TYPE, v));
        Optional.ofNullable(clientAssertion).ifPresent(v -> put(FormKeys.CLIENT_ASSERTION, v));
        Optional.ofNullable(scope).ifPresent(v -> put(FormKeys.SCOPE, v));
        Optional.ofNullable(redirectUri).ifPresent(v -> put(FormKeys.REDIRECT_URI, v));
        Optional.ofNullable(code).ifPresent(v -> put(FormKeys.CODE, v));
        Optional.ofNullable(refreshToken).ifPresent(v -> put(FormKeys.REFRESH_TOKEN, v));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String grantType;
        private String scope;
        private String clientAssertionType;
        private String clientAssertion;
        private String redirectUri;
        private String code;
        private String refreshToken;

        public Builder setGrantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder setScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder setClientAssertionType(String clientAssertionType) {
            this.clientAssertionType = clientAssertionType;
            return this;
        }

        public Builder setClientAssertion(String clientAssertion) {
            this.clientAssertion = clientAssertion;
            return this;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public GetTokenForm build() {
            return new GetTokenForm(
                    this.grantType,
                    this.scope,
                    this.clientAssertionType,
                    this.clientAssertion,
                    this.redirectUri,
                    this.code,
                    this.refreshToken);
        }
    }
}
