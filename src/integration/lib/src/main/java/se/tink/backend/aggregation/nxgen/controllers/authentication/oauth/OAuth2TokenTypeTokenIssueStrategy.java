package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Map;
import java.util.Optional;

public class OAuth2TokenTypeTokenIssueStrategy implements OAuth2TokenIssueStrategy {

    private final OAuth2AuthorizationSpecification authorizationParamProvider;

    public OAuth2TokenTypeTokenIssueStrategy(
            OAuth2AuthorizationSpecification auth2AuthorizationParamProvider) {
        this.authorizationParamProvider = auth2AuthorizationParamProvider;
    }

    @Override
    public OAuth2Token issueToken(Map<String, String> authorizationResponseParams) {
        OAuth2Token token = new OAuth2Token(System.currentTimeMillis() / 1000);
        token.setAccessToken(authorizationResponseParams.get("access_token"));
        token.setExpiresIn(
                Optional.ofNullable(authorizationResponseParams.get("expires_in"))
                        .map(v -> new Long(v))
                        .orElseGet(
                                () ->
                                        authorizationParamProvider
                                                .getDefaultAccessTokenLifetime()
                                                .orElseThrow(
                                                        () ->
                                                                new IllegalStateException(
                                                                        "Access token lifetime can't be find neither in authorization server response, neither in authorization params provider"))));
        token.setTokenType(authorizationResponseParams.get("token_type"));
        token.setScope(authorizationResponseParams.get("scope"));
        return token;
    }
}
