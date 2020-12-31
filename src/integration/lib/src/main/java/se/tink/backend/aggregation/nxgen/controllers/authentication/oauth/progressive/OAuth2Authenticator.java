package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.progressive;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2AuthorizationServerClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2AuthorizationServerStandardClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2TokenStorageDefaultImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OAuth2Authenticator extends StatelessProgressiveAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String REFRESH_TOKEN_STEP_ID = "refreshAccessTokenStep";
    private static final String AUTOMATIC_AUTHORIZATION_STEP_ID = "authorizationStep";
    static final String AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID = "issueAccessTokenStep";
    private final List<AuthenticationStep> authSteps;
    private final OAuth2TokenStorage tokenStorage;
    private final OAuth2AuthorizationServerClient authorizationServerClient;

    public OAuth2Authenticator(
            final OAuth2AuthorizationSpecification authorizationSpecification,
            final PersistentStorage persistentStorage,
            final TinkHttpClient httpClient,
            final StrongAuthenticationState strongAuthenticationState) {
        this(
                authorizationSpecification,
                new OAuth2TokenStorageDefaultImpl(persistentStorage),
                httpClient,
                strongAuthenticationState);
    }

    public OAuth2Authenticator(
            final OAuth2AuthorizationSpecification authorizationSpecification,
            final OAuth2TokenStorage tokenStorage,
            final TinkHttpClient httpClient,
            final StrongAuthenticationState strongAuthenticationState) {
        this(
                new OAuth2AuthorizationServerStandardClient(
                        httpClient, authorizationSpecification, strongAuthenticationState),
                tokenStorage);
    }

    public OAuth2Authenticator(
            final OAuth2AuthorizationServerClient authorizationServerClient,
            final OAuth2TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
        this.authorizationServerClient = authorizationServerClient;
        this.authSteps = createAuthenticationSteps();
    }

    private List<AuthenticationStep> createAuthenticationSteps() {
        return ImmutableList.of(
                new AutomaticAuthenticationStep(
                        this::authorizationStepHandler, AUTOMATIC_AUTHORIZATION_STEP_ID),
                new AutomaticAuthenticationStep(
                        this::refreshTokenStepHandler, REFRESH_TOKEN_STEP_ID),
                createOAuth2ThirdPartyAppStep());
    }

    private ThirdPartyAppAuthenticationStep createOAuth2ThirdPartyAppStep() {
        return new ThirdPartyAppAuthenticationStep(
                AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID,
                authorizationServerClient.getAuthorizationEndpointPayload(),
                authorizationServerClient.getWaitingForResponseConfiguration(),
                this::handleIssueAccessTokenStep);
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authSteps;
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
                logger.debug(ex.getMessage(), ex);
                return AuthenticationStepResponse.executeStepWithId(
                        AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID);
            }
        }
        return AuthenticationStepResponse.executeStepWithId(
                AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID);
    }

    private AuthenticationStepResponse handleIssueAccessTokenStep(
            Map<String, String> callbackData) {
        OAuth2Token token = authorizationServerClient.handleAuthorizationResponse(callbackData);
        tokenStorage.storeToken(token);
        logger.info(
                "OAuth2 token with expiration in " + token.getExpiresIn() + " seconds was stored");
        return AuthenticationStepResponse.executeNextStep();
    }

    public Optional<OAuth2Token> getToken() {
        return tokenStorage.fetchToken();
    }
}
