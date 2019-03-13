package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankAuthenticator implements OAuth2Authenticator {
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SwedbankAuthenticator(SwedbankApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        OAuth2Token token = apiClient.refreshToken(refreshToken);
        persistentStorage.put(SwedbankConstants.StorageKeys.OAUTH_TOKEN, token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(SwedbankConstants.StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
