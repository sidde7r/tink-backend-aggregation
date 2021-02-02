package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.executor.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26CryptoService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlApiCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlApiCallParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlApiParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token.N26FetchTokenParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token.N26OAuth2RedirectFetchTokenCall;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentRemoteInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.StrongAuthenticationState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStore;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class N26OauthPaymentAuthenticator implements OAuth2Authenticator {
    private final AgentAuthenticationPersistedData agentAuthenticationPersistedData =
            new AgentAuthenticationPersistedData(new HashMap<>());
    private final AuthenticationPersistedDataCookieStore authenticationPersistedDataCookieStore =
            AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                    agentAuthenticationPersistedData);
    private final AgentAuthenticationProcessState authenticationProcessState =
            new AgentAuthenticationProcessState(new HashMap<>());
    private final Xs2aDevelopersProviderConfiguration configuration;
    private final N26FetchAuthorizationUrlApiCall fetchAuthorizationUrlApiCall;
    private final N26OAuth2RedirectFetchTokenCall auth2RedirectFetchTokenCall;
    private final N26CryptoService n26CryptoService;
    private final AgentExtendedClientInfo agentExtendedClientInfo;
    private final ObjectMapper objectMapper;

    public N26OauthPaymentAuthenticator(
            AgentHttpClient agentHttpClient,
            Xs2aDevelopersProviderConfiguration configuration,
            CredentialsRequest credentialsRequest,
            ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.n26CryptoService = new N26CryptoService();

        this.fetchAuthorizationUrlApiCall = prepareFetchAuthorizationUrlApiCall(agentHttpClient);
        this.auth2RedirectFetchTokenCall = prepareFetchTokenApiCall(agentHttpClient);

        this.agentExtendedClientInfo =
                AgentExtendedClientInfo.builder()
                        .clientInfo(
                                AgentClientInfo.builder()
                                        .appId(credentialsRequest.getState())
                                        .build())
                        .build();
    }

    private N26OAuth2RedirectFetchTokenCall prepareFetchTokenApiCall(
            AgentHttpClient agentHttpClient) {
        N26FetchTokenParameters fetchTokenParameters =
                N26FetchTokenParameters.builder()
                        .baseUrl(N26Constants.Url.BASE_URL)
                        .redirectUrl(configuration.getRedirectUrl())
                        .clientId(configuration.getClientId())
                        .scope(N26Constants.QueryValues.PISP_SCOPE)
                        .build();
        return new N26OAuth2RedirectFetchTokenCall(
                agentHttpClient, fetchTokenParameters, objectMapper);
    }

    private N26FetchAuthorizationUrlApiCall prepareFetchAuthorizationUrlApiCall(
            AgentHttpClient agentHttpClient) {
        N26FetchAuthorizationUrlApiParameters apiParameters =
                N26FetchAuthorizationUrlApiParameters.builder()
                        .baseUrl(N26Constants.Url.BASE_URL)
                        .scope(N26Constants.QueryValues.PISP_SCOPE)
                        .build();
        return new N26FetchAuthorizationUrlApiCall(agentHttpClient, apiParameters);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        N26ProcessStateAccessor n26ProcessStateAccessor =
                new N26ProcessStateAccessor(authenticationProcessState, objectMapper);
        N26ProcessStateData n26ProcessStateData = n26ProcessStateAccessor.getN26ProcessStateData();

        N26FetchAuthorizationUrlApiCallParameters apiCallParameters =
                prepareAuthorizationParameters(n26ProcessStateData);
        ExternalApiCallResult<URI> executeResult =
                fetchAuthorizationUrlApiCall.execute(
                        apiCallParameters, null, authenticationPersistedDataCookieStore);

        handleBankServiceError(executeResult);

        executeResult.getResponse().ifPresent(n26ProcessStateData::setAuthorizationUri);
        n26ProcessStateAccessor.storeN26ProcessStateData(n26ProcessStateData);

        URI uri = executeResult.getResponse().orElseThrow(IllegalStateException::new);
        return URL.of(uri.toString());
    }

    @VisibleForTesting
    N26FetchAuthorizationUrlApiCallParameters prepareAuthorizationParameters(
            N26ProcessStateData n26ProcessStateData) {
        String codeVerifier = n26CryptoService.generateCodeVerifier();
        n26ProcessStateData.setCodeVerifier(codeVerifier);
        String strongState =
                StrongAuthenticationState.getState(agentExtendedClientInfo.getClientInfo());
        return N26FetchAuthorizationUrlApiCallParameters.builder()
                .clientId(configuration.getClientId())
                .codeChallenge(n26CryptoService.generateCodeChallenge(codeVerifier))
                .redirectUri(configuration.getRedirectUrl())
                .state(strongState)
                .build();
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        RedirectFetchTokenCallAuthenticationParameters parameters =
                prepareAccessTokenParameter(code);
        ExternalApiCallResult<RefreshableAccessToken> apiCallResult =
                auth2RedirectFetchTokenCall.execute(
                        parameters,
                        agentExtendedClientInfo,
                        authenticationPersistedDataCookieStore);
        handleBankServiceError(apiCallResult);

        RefreshableAccessToken redirectTokens =
                apiCallResult.getResponse().orElseThrow(IllegalStateException::new);
        return new OAuth2Token(
                redirectTokens.getAccessToken().getTokenType(),
                new String(redirectTokens.getAccessToken().getBody(), StandardCharsets.UTF_8),
                null,
                null,
                redirectTokens.getAccessToken().getExpiresInSeconds(),
                0,
                redirectTokens.getAccessToken().getIssuedAtInSeconds());
    }

    @VisibleForTesting
    RedirectFetchTokenCallAuthenticationParameters prepareAccessTokenParameter(String code) {
        String state = StrongAuthenticationState.getState(agentExtendedClientInfo.getClientInfo());
        Map<String, String> cache = new HashMap<>();
        cache.put("state", state);
        cache.put("code", code);
        AgentRemoteInteractionData agentRemoteInteractionData =
                new AgentRemoteInteractionData(cache);
        return new RedirectFetchTokenCallAuthenticationParameters(
                agentRemoteInteractionData,
                agentAuthenticationPersistedData,
                authenticationProcessState);
    }

    private void handleBankServiceError(ExternalApiCallResult apiCallResult) {
        Optional<AgentBankApiError> optionalBankError = apiCallResult.getAgentBankApiError();
        if (optionalBankError.isPresent()) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    optionalBankError.get().getDetails().getErrorMessage());
        }
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        // PISP flow does not provide refresh tokens
        throw new IllegalStateException();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        // Token already in the persistent session
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        // No known specific callback so far
    }
}
