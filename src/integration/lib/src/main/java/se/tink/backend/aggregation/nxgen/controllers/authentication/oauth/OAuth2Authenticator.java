package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class OAuth2Authenticator extends StatelessProgressiveAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2Authenticator.class);
    private static final String REFRESH_TOKEN_STEP_ID = "refreshAccessTokenStep";
    static final String AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID = "issueAccessTokenStep";
    private List<AuthenticationStep> authSteps = new LinkedList<>();
    private final OAuth2TokenStorage tokenStorage;
    private final OAuth2AuthorizationServerClient authorizationServerClient;

    public OAuth2Authenticator(
            final OAuth2AuthorizationSpecification authorizationSpecification,
            final PersistentStorage persistentStorage,
            final TinkHttpClient httpClient) {
        this(
                authorizationSpecification,
                new OAuth2TokenStorageDefaultImpl(persistentStorage),
                httpClient);
    }

    public OAuth2Authenticator(
            final OAuth2AuthorizationSpecification authorizationSpecification,
            final OAuth2TokenStorage tokenStorage,
            final TinkHttpClient httpClient) {
        this(
                new OAuth2AuthorizationServerStandardClient(httpClient, authorizationSpecification),
                tokenStorage);
    }

    public OAuth2Authenticator(
            final OAuth2AuthorizationServerClient authorizationServerClient,
            final OAuth2TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
        this.authorizationServerClient = authorizationServerClient;
        authSteps.add(
                new AutomaticAuthenticationStep(
                        this::authorizationStepHandler, "authorizationStep"));
        authSteps.add(
                new AutomaticAuthenticationStep(
                        this::refreshTokenStepHandler, REFRESH_TOKEN_STEP_ID));
        authSteps.add(createOAuth2ThirdPartyAppStep());
    }

    private ThirdPartyAppAuthenticationStep createOAuth2ThirdPartyAppStep() {
        return new ThirdPartyAppAuthenticationStep(
                AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID,
                authorizationServerClient.getAuthorizationEndpointPayload(),
                authorizationServerClient.getWaitingForResponseConfiguration(),
                this::handleIssueAccessTokenStep);
    }

    @Override
    public List<? extends AuthenticationStep> authenticationSteps() {
        return authSteps;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return false;
    }

    private AuthenticationStepResponse authorizationStepHandler() {
        return tokenStorage
                .fetchToken()
                .map(
                        token ->
                                token.isValid()
                                        ? AuthenticationStepResponse.authenticationSucceeded()
                                        : AuthenticationStepResponse.executeStepWithId(
                                                REFRESH_TOKEN_STEP_ID))
                .orElse(
                        AuthenticationStepResponse.executeStepWithId(
                                AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID));
    }

    private AuthenticationStepResponse refreshTokenStepHandler() {
        OAuth2Token token = tokenStorage.fetchToken().get();
        if (token.canRefresh()) {
            try {
                tokenStorage.storeToken(
                        authorizationServerClient.refreshAccessToken(token.getRefreshToken()));
                return AuthenticationStepResponse.authenticationSucceeded();
            } catch (HttpResponseException | HttpClientException ex) {
                LOG.debug(ex.getMessage(), ex);
                return AuthenticationStepResponse.executeStepWithId(
                        AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID);
            }
        }
        return AuthenticationStepResponse.executeStepWithId(
                AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID);
    }

    private AuthenticationStepResponse handleIssueAccessTokenStep(
            Map<String, String> callbackData) {
        tokenStorage.storeToken(
                authorizationServerClient.handleAuthorizationResponse(callbackData));
        return AuthenticationStepResponse.executeNextStep();
    }
}
