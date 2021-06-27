package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.util.LinkedMultiValueMap;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth.PaymentAccessToken;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets.StarlingSecrets;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class StarlingPaymentAuthenticator implements OAuth2Authenticator {
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String REDIRECT_KEY = "redirect_uri";
    private static final String SCOPE_KEY = "scope";
    private static final String STATE_KEY = "state";
    private static final String RESPONSE_TYPE_KEY = "response_type";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String FORMATTER =
            "code=%s&grant_type=authorization_code&redirect_uri=%s&client_secret=%s&client_id=%s";
    private final StarlingSecrets configurationEntity;
    private final AgentHttpClient httpClient;
    private final String redirectUrl;

    public StarlingPaymentAuthenticator(
            AgentConfiguration<StarlingSecrets> agentConfiguration,
            AgentHttpClient agentHttpClient) {
        configurationEntity = agentConfiguration.getProviderSpecificConfiguration();
        this.httpClient = agentHttpClient;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        String scope =
                String.join(
                        " ",
                        "account-list:read",
                        "account-identifier:read",
                        "pay-local-once:create",
                        "pay-local:read");

        return URL.of(
                UriBuilder.fromUri(StarlingConstants.Url.AUTH_STARLING)
                        .queryParam(CLIENT_ID_KEY, configurationEntity.getAisClientId())
                        .queryParam(REDIRECT_KEY, redirectUrl)
                        .queryParam(RESPONSE_TYPE_KEY, RESPONSE_TYPE_VALUE)
                        .queryParam(SCOPE_KEY, scope)
                        .queryParam(STATE_KEY, state)
                        .build()
                        .toString());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(StarlingConstants.HeaderKey.CONTENT, MediaType.APPLICATION_FORM_URLENCODED);

        String payload =
                String.format(
                        FORMATTER,
                        code,
                        redirectUrl,
                        configurationEntity.getAisClientSecret(),
                        configurationEntity.getAisClientId());
        return Objects.requireNonNull(
                        httpClient
                                .exchange(
                                        new RequestEntity<>(
                                                payload,
                                                headers,
                                                HttpMethod.POST,
                                                StarlingConstants.Url.GET_ACCESS_TOKEN.toUri()),
                                        PaymentAccessToken.class,
                                        null)
                                .getBody())
                .toOauth2Token();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        // PISP flow does not provide refresh tokens
        throw new IllegalStateException();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        // no callback here, the usage will be at controller
        throw new IllegalStateException();
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        // the logic is at controller
    }
}
