package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import com.google.common.base.Preconditions;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class OAuth2CodeTypeTokenIssueStrategy implements OAuth2TokenIssueStrategy {

    private final OAuth2AuthorizationSpecification authorizationSpecification;
    private final TinkHttpClient httpClient;
    private final OAuth2TokenResponseParser tokenResponseParser;

    public OAuth2CodeTypeTokenIssueStrategy(
            OAuth2AuthorizationSpecification authorizationParamProvider,
            TinkHttpClient tinkHttpClient) {
        this(
                authorizationParamProvider,
                tinkHttpClient,
                new OAuth2TokenResponseStandardParser(
                        authorizationParamProvider.getDefaultAccessTokenLifetime().orElse(null),
                        authorizationParamProvider
                                .getAccessTokenResponseClientSpecificProperties()));
    }

    public OAuth2CodeTypeTokenIssueStrategy(
            OAuth2AuthorizationSpecification authorizationParamProvider,
            TinkHttpClient tinkHttpClient,
            OAuth2TokenResponseParser tokenResponseParser) {
        this.authorizationSpecification = authorizationParamProvider;
        this.httpClient = tinkHttpClient;
        this.tokenResponseParser = tokenResponseParser;
    }

    @Override
    public OAuth2Token issueToken(Map<String, String> authorizationResponseParams) {
        final String codeParam = "code";
        Preconditions.checkArgument(
                authorizationResponseParams.containsKey(codeParam),
                "Authorization response has to contain 'code' parameter");
        String tokenRawResponse =
                httpClient
                        .request(
                                authorizationSpecification
                                        .getAccessTokenEndpoint()
                                        .getUrl()
                                        .toString())
                        .headers(authorizationSpecification.getAccessTokenEndpoint().getHeaders())
                        .body(
                                buildBody(authorizationResponseParams.get("code")),
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(String.class);
        return tokenResponseParser.parse(tokenRawResponse);
    }

    protected String buildBody(String code) {
        Form.Builder formBuilder =
                Form.builder()
                        .put("grant_type", "authorization_code")
                        .put("code", code)
                        .put("redirect_uri", authorizationSpecification.getRedirectUrl().toString())
                        .put("client_id", authorizationSpecification.getClientId());
        authorizationSpecification.getAccessTokenEndpoint().getClientSpecificParameters().entrySet()
                .stream()
                .forEach(entry -> formBuilder.put(entry.getKey(), entry.getValue()));
        return formBuilder.build().serialize();
    }
}
