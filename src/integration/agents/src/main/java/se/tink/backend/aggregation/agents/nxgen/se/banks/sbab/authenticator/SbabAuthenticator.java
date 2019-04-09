package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SbabAuthenticator implements OAuth2Authenticator {
    private final SbabApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SbabAuthenticator(SbabApiClient client, SessionStorage sessionStorage) {
        this.apiClient = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String pendingCode) {
        return apiClient.getAccessToken(pendingCode).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token oauth2token) {
        sessionStorage.put(StorageKeys.OAUTH2_TOKEN, oauth2token);
        sessionStorage.put(StorageKeys.ACCESS_TOKEN, oauth2token.getAccessToken());
    }
}
