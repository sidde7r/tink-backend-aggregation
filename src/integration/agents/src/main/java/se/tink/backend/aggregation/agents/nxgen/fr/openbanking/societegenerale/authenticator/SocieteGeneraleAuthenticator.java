package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SocieteGeneraleAuthenticator implements OAuth2Authenticator {

    private final SocieteGeneraleApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SocieteGeneraleConfiguration societeGeneraleConfiguration;
    private final String redirectUrl;

    public SocieteGeneraleAuthenticator(
            SocieteGeneraleApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<SocieteGeneraleConfiguration> agentConfiguration) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.societeGeneraleConfiguration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final Form params =
                Form.builder()
                        .put(SocieteGeneraleConstants.QueryKeys.STATE, state)
                        .put(
                                SocieteGeneraleConstants.QueryKeys.CLIENT_ID,
                                societeGeneraleConfiguration.getClientId())
                        .put(
                                SocieteGeneraleConstants.QueryKeys.SCOPE,
                                SocieteGeneraleConstants.QueryValues.SCOPE)
                        .put(
                                SocieteGeneraleConstants.QueryKeys.RESPONSE_TYPE,
                                SocieteGeneraleConstants.QueryValues.RESPONSE_TYPE)
                        .build();
        return new URL(
                SocieteGeneraleConstants.Urls.AUTHORIZE_PATH.concat("?").concat(params.toString()));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenRequest request =
                TokenRequest.builder()
                        .setGrantType(SocieteGeneraleConstants.QueryValues.AUTHORIZATION_CODE)
                        .setRedirectUri(redirectUrl)
                        .setCode(code)
                        .build();

        TokenResponse response = apiClient.exchangeAuthorizationCodeOrRefreshToken(request);
        persistentStorage.put(
                SocieteGeneraleConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        TokenRequest request =
                TokenRequest.builder()
                        .setGrantType(SocieteGeneraleConstants.QueryValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .buildRefresh();

        TokenResponse response = apiClient.exchangeAuthorizationCodeOrRefreshToken(request);
        persistentStorage.put(
                SocieteGeneraleConstants.StorageKeys.TOKEN, response.getAccessToken());
        return response.toOauthToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(SocieteGeneraleConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
