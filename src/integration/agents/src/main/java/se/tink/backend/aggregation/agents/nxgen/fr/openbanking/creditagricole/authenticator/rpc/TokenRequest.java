package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.rpc;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {

    private String scope;
    private String grantType;
    private String refreshToken;
    private String code;
    private String redirectUri;
    private String codeVerifier;
    private String clientId;

    public TokenRequest(
            String scope,
            String grantType,
            String refreshToken,
            String code,
            String redirectUri,
            String codeVerifier,
            String clientId) {
        this.scope = scope;
        this.grantType = grantType;
        this.refreshToken = refreshToken;
        this.code = code;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
        this.clientId = clientId;
    }

    public static class TokenRequestBuilder {
        private String scope;
        private String grantType;
        private String refreshToken;
        private String code;
        private String redirectUri;
        private String codeVerifier;
        private String clientId;

        public TokenRequestBuilder code(String code) {
            this.code = code;
            return this;
        }

        public TokenRequestBuilder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public TokenRequestBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenRequestBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenRequestBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public TokenRequestBuilder codeVerifier(String codeVerifier) {
            this.codeVerifier = codeVerifier;
            return this;
        }

        public TokenRequest build() {
            return new TokenRequest(
                    scope, grantType, refreshToken, code, redirectUri, codeVerifier, clientId);
        }
    }

    public String toData() {

        Form.Builder builder = Form.builder();
        append(builder, CreditAgricoleConstants.QueryKeys.SCOPE, scope);
        append(builder, CreditAgricoleConstants.QueryKeys.GRANT_TYPE, grantType);
        append(builder, CreditAgricoleConstants.QueryKeys.REFRESH_TOKEN, refreshToken);
        append(builder, CreditAgricoleConstants.QueryKeys.CODE, code);
        append(builder, CreditAgricoleConstants.QueryKeys.REDIRECT_URI, redirectUri);
        append(builder, CreditAgricoleConstants.QueryKeys.CLIENT_ID, clientId);
        return builder.build().serialize();
    }

    private void append(Form.Builder builder, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            builder.put(key, value);
        }
    }
}
