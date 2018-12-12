package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Uris;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthGrantTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthResponseTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthScopeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.PendingAuthCodeRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SbabAuthenticator implements OAuth2Authenticator {
    private final SbabApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public SbabAuthenticator(
            SbabApiClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final PendingAuthCodeRequest request =
                new PendingAuthCodeRequest()
                        .withClientId(persistentStorage.get(StorageKey.CLIENT_ID))
                        .withResponseType(AuthResponseTypeEntity.PENDING_CODE)
                        .withRedirectUri(persistentStorage.get(StorageKey.REDIRECT_URI))
                        .withScope(AuthScopeEntity.ACCOUNT_LOAN_READ)
                        .withState(state)
                        .withAuthMethod(AuthMethodEntity.MOBILE_BANKID)
                        .withUserId(persistentStorage.get(StorageKey.USER_ID));

        return new URL(Uris.GET_PENDING_AUTH_CODE(Environment.PRODUCTION)).queryParams(request);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String pendingCode) {
        final AccessTokenRequest accessTokenRequest =
                new AccessTokenRequest()
                        .withGrantType(AuthGrantTypeEntity.PENDING_AUTHORIZATION_CODE)
                        .withPendingCode(pendingCode)
                        .withRedirectUri(persistentStorage.get(StorageKey.REDIRECT_URI));

        return apiClient.getAccessToken(accessTokenRequest).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        return null;
    }

    @Override
    public void useAccessToken(OAuth2Token oauth2token) {
        sessionStorage.put(StorageKey.OAUTH2_TOKEN, oauth2token);
        sessionStorage.put(StorageKey.ACCESS_TOKEN, oauth2token.getAccessToken());
    }
}
