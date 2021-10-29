package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.FormKeys.AUTHORIZATION_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.FormKeys.REFRESH_TOKEN;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.configuration.ArkeaConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ArkeaAuthenticator implements OAuth2Authenticator {

    private final ArkeaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final ArkeaConfiguration arkeaConfiguration;
    private final String redirectUrl;

    public ArkeaAuthenticator(
            ArkeaApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<ArkeaConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.arkeaConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return Urls.AUTHORISATION_PATH
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE_RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, arkeaConfiguration.getClientKey())
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.SCOPE, QueryValues.AISP_EXTENDED_TRANSACTION_HISTORY_SCOPE)
                .queryParam(QueryKeys.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {

        ArkeaGetTokenRequest getTokenRequest =
                new ArkeaGetTokenRequest(
                        arkeaConfiguration.getClientKey(), AUTHORIZATION_CODE, redirectUrl, code);
        ArkeaTokenResponse tokenResponse = apiClient.exchangeAuthorizationCode(getTokenRequest);
        return createOAuth2TokenFromTokenResponse(tokenResponse);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        ArkeaRefreshTokenRequest refreshTokenRequest =
                new ArkeaRefreshTokenRequest(REFRESH_TOKEN, refreshToken);
        ArkeaTokenResponse tokenResponse = apiClient.refreshAccessToken(refreshTokenRequest);
        return createOAuth2TokenFromTokenResponse(tokenResponse);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private OAuth2Token createOAuth2TokenFromTokenResponse(ArkeaTokenResponse tokenResponse) {
        return OAuth2Token.create(
                tokenResponse.getTokenType(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }
}
