package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RefreshTokenRequest {

    @JsonProperty private String code;
    @JsonProperty private String scope;

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private RefreshTokenRequest(
            String code,
            String grantType,
            String redirectUri,
            String clientId,
            String clientSecret,
            String scope,
            String refreshToken) {
        this.code = code;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.refreshToken = refreshToken;
    }

    public static TokenRequestBuilder builder() {
        return new TokenRequestBuilder();
    }

    public String getCode() {
        return code;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public static class TokenRequestBuilder {
        private String code;
        private String grantType;
        private String redirectUri;
        private String clientId;
        private String clientSecret;
        private String scope;
        private String refreshToken;

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

        public TokenRequestBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenRequestBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public TokenRequestBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public RefreshTokenRequest build() {
            return new RefreshTokenRequest(
                    code, grantType, redirectUri, clientId, clientSecret, scope, refreshToken);
        }
    }
}
