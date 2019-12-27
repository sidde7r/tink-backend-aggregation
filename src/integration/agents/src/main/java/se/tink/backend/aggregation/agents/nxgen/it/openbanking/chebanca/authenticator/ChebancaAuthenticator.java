package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.GET_TOKEN_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.LOGIN_REDIRECT_FAILED;

import javax.servlet.http.HttpServletResponse;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.detail.AuthorizationURLBuilder;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ChebancaAuthenticator implements OAuth2Authenticator {

    private final ChebancaApiClient apiClient;
    private final ChebancaConfiguration configuration;
    private AuthorizationURLBuilder authorizationUrlBuilder;

    public ChebancaAuthenticator(
            ChebancaApiClient apiClient,
            ChebancaConfiguration configuration,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = requireNonNull(apiClient);
        this.configuration = requireNonNull(configuration);
        requireNonNull(strongAuthenticationState);
        this.authorizationUrlBuilder =
                createAuthorizationUrlBuilder(strongAuthenticationState, configuration);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final URL authorizationUrl = authorizationUrlBuilder.buildAuthorizationURL();
        HttpResponse loginUrlResponse = apiClient.getLoginUrl(authorizationUrl);
        HttpResponseChecker.checkIfSuccessfulResponse(
                loginUrlResponse, HttpServletResponse.SC_FOUND, LOGIN_REDIRECT_FAILED);
        String loginUrlAsString = loginUrlResponse.getLocation().toString();
        return new URL(loginUrlAsString);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        TokenRequest tokenRequest =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        code,
                        FormValues.AUTHORIZATION_CODE,
                        configuration.getRedirectUrl());

        HttpResponse response = apiClient.createToken(tokenRequest);

        HttpResponseChecker.checkIfSuccessfulResponse(
                response, HttpServletResponse.SC_OK, GET_TOKEN_FAILED);

        return response.getBody(TokenResponse.class).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        TokenRequest tokenRequest =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        refreshToken,
                        FormValues.REFRESH_TOKEN);
        HttpResponse response = apiClient.createToken(tokenRequest);
        HttpResponseChecker.checkIfSuccessfulResponse(
                response, HttpServletResponse.SC_OK, GET_TOKEN_FAILED);

        return response.getBody(TokenResponse.class).toTinkToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        apiClient.save(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private AuthorizationURLBuilder createAuthorizationUrlBuilder(
            StrongAuthenticationState strongAuthenticationState,
            ChebancaConfiguration chebancaConfig) {
        return new AuthorizationURLBuilder(
                chebancaConfig.getClientId(),
                chebancaConfig.getRedirectUrl(),
                strongAuthenticationState.getState());
    }
}
