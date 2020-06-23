package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CmcicAuthenticator implements OAuth2Authenticator {

    private final CmcicApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public CmcicAuthenticator(CmcicApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.getAispToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token accessToken = apiClient.refreshToken(refreshToken);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
        return accessToken;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
