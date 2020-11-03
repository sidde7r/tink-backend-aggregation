package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class OAuth2AuthorizationServerStandardClient implements OAuth2AuthorizationServerClient {

    private static final long DEFAULT_WAIT_FOR_MINUTES = 9;

    private final TinkHttpClient httpClient;
    private final OAuth2AuthorizationSpecification authorizationSpecification;
    private final long waitDuration;
    private final TimeUnit waitTimeUnit;
    private final StrongAuthenticationState strongAuthenticationState;
    private final OAuth2TokenIssueStrategy codeTypeTokenIssueStrategy;
    private final OAuth2TokenIssueStrategy tokenTypeTokenIssueStrategy;
    private final OAuth2TokenResponseParser tokenResponseParser;

    public OAuth2AuthorizationServerStandardClient(
            TinkHttpClient httpClient,
            OAuth2AuthorizationSpecification authorizationSpecification,
            StrongAuthenticationState strongAuthenticationState) {
        this.httpClient = httpClient;
        this.authorizationSpecification = authorizationSpecification;
        this.waitDuration = DEFAULT_WAIT_FOR_MINUTES;
        this.waitTimeUnit = TimeUnit.MINUTES;
        this.strongAuthenticationState = strongAuthenticationState;
        this.tokenResponseParser =
                new OAuth2TokenResponseStandardParser(
                        authorizationSpecification.getDefaultAccessTokenLifetime().orElse(null),
                        authorizationSpecification
                                .getAccessTokenResponseClientSpecificProperties());
        this.codeTypeTokenIssueStrategy =
                new OAuth2CodeTypeTokenIssueStrategy(
                        authorizationSpecification, httpClient, tokenResponseParser);
        this.tokenTypeTokenIssueStrategy =
                new OAuth2TokenTypeTokenIssueStrategy(authorizationSpecification);
    }

    OAuth2AuthorizationServerStandardClient(
            TinkHttpClient httpClient,
            OAuth2AuthorizationSpecification authorizationSpecification,
            long waitDuration,
            TimeUnit waitTimeUnit,
            OAuth2TokenIssueStrategy codeTypeTokenIssueStrategy,
            OAuth2TokenIssueStrategy tokenTypeTokenIssueStrategy,
            StrongAuthenticationState strongAuthenticationState) {
        this.httpClient = httpClient;
        this.authorizationSpecification = authorizationSpecification;
        this.waitDuration = waitDuration;
        this.waitTimeUnit = waitTimeUnit;
        this.strongAuthenticationState = strongAuthenticationState;
        this.codeTypeTokenIssueStrategy = codeTypeTokenIssueStrategy;
        this.tokenTypeTokenIssueStrategy = tokenTypeTokenIssueStrategy;
        this.tokenResponseParser =
                new OAuth2TokenResponseStandardParser(
                        authorizationSpecification.getDefaultAccessTokenLifetime().orElse(null),
                        authorizationSpecification
                                .getAccessTokenResponseClientSpecificProperties());
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAuthorizationEndpointPayload() {
        return ThirdPartyAppAuthenticationPayload.of(createAuthorizationURL());
    }

    @Override
    public SupplementalWaitRequest getWaitingForResponseConfiguration() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(), waitDuration, waitTimeUnit);
    }

    @Override
    public OAuth2Token handleAuthorizationResponse(Map<String, String> callbackData)
            throws OAuth2AuthorizationException {
        checkForAuthorizationError(callbackData);
        if (authorizationSpecification.isResponseTypeCode()) {
            return codeTypeTokenIssueStrategy.issueToken(callbackData);
        } else {
            return tokenTypeTokenIssueStrategy.issueToken(callbackData);
        }
    }

    @Override
    public OAuth2Token refreshAccessToken(final String refreshToken) {
        String tokenRawResponse =
                httpClient
                        .request(
                                authorizationSpecification
                                        .getRefreshTokenEndpoint()
                                        .getUrl()
                                        .toString())
                        .headers(authorizationSpecification.getRefreshTokenEndpoint().getHeaders())
                        .body(
                                buildRefreshTokenRequestBody(refreshToken),
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(String.class);
        OAuth2Token newToken = tokenResponseParser.parse(tokenRawResponse);
        if (!newToken.canRefresh()) {
            newToken.setRefreshToken(refreshToken);
        }
        return newToken;
    }

    private URL createAuthorizationURL() {
        try {
            URIBuilder uriBuilder =
                    new URIBuilder(
                                    authorizationSpecification
                                            .getAuthorizationEndpoint()
                                            .getUrl()
                                            .toURI())
                            .addParameter(
                                    OAuth2AuthorizationUrlParam.RESPONSE_TYPE,
                                    authorizationSpecification.getResponseType())
                            .addParameter(
                                    OAuth2AuthorizationUrlParam.CLIENT_ID,
                                    authorizationSpecification.getClientId())
                            .addParameter(
                                    OAuth2AuthorizationUrlParam.REDIRECT_URI,
                                    authorizationSpecification.getRedirectUrl().toString())
                            .addParameter(
                                    OAuth2AuthorizationUrlParam.STATE,
                                    strongAuthenticationState.getState());
            authorizationSpecification
                    .getScope()
                    .ifPresent(
                            scope ->
                                    uriBuilder.addParameter(
                                            OAuth2AuthorizationUrlParam.SCOPE, scope));
            return new URL(uriBuilder.build().toURL().toString());
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String buildRefreshTokenRequestBody(final String refreshToken) {
        Form.Builder formBuilder =
                Form.builder()
                        .put("grant_type", "refresh_token")
                        .put("refresh_token", refreshToken);
        authorizationSpecification.getScope().ifPresent(scope -> formBuilder.put("scope", scope));
        authorizationSpecification.getRefreshTokenEndpoint().getClientSpecificParameters()
                .entrySet().stream()
                .forEach(entry -> formBuilder.put(entry.getKey(), entry.getValue()));
        return formBuilder.build().serialize();
    }

    private void checkForAuthorizationError(Map<String, String> callbackData) {
        final String errorKey = "error";
        final String errorDescriptionKey = "error_description";
        if (callbackData.containsKey(errorKey)) {
            throw new OAuth2AuthorizationException(
                    callbackData.get(errorKey), callbackData.get(errorDescriptionKey));
        }
    }
}
