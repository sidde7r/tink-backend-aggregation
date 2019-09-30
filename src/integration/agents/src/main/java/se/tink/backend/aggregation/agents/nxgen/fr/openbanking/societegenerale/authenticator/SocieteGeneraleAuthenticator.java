package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SocieteGeneraleAuthenticator implements OAuth2Authenticator {

    private final SocieteGeneraleApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SocieteGeneraleConfiguration societeGeneraleConfiguration;

    public SocieteGeneraleAuthenticator(
            SocieteGeneraleApiClient apiClient,
            PersistentStorage persistentStorage,
            SocieteGeneraleConfiguration societeGeneraleConfiguration) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.societeGeneraleConfiguration = societeGeneraleConfiguration;
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
                SocieteGeneraleConstants.Urls.AUTHORTIZE_PATH
                        .concat("?")
                        .concat(params.toString()));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenRequest request =
                TokenRequest.builder()
                        .setGrantType(SocieteGeneraleConstants.QueryValues.AUTHORIZATION_CODE)
                        .setRedirectUri(societeGeneraleConfiguration.getRedirectUrl())
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
