package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class LansforsakringarSessionHandler implements SessionHandler {

    private final LansforsakringarApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token token = fetchTokenFromPermanentStorage();

        if (token.hasAccessExpired()) {
            refreshAndStoreNewToken(token);
        }

        try {
            apiClient.getAccounts();
        } catch (BankServiceException e) {
            removeTokenFromStorageAndThrowSessionError();
        }
    }

    private OAuth2Token refreshAndStoreNewToken(OAuth2Token token) throws SessionException {
        OAuth2Token refreshToken =
                apiClient.refreshToken(
                        token.getRefreshToken()
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception));
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, refreshToken);
        sessionStorage.put(LansforsakringarConstants.StorageKeys.ACCESS_TOKEN, refreshToken);
        return refreshToken;
    }

    private OAuth2Token fetchTokenFromPermanentStorage() throws SessionException {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    /*
     * With lansforsakringar we can refresh the token but have a expired consentId which will cause the agent to think we have a valid session.
     * Therefore, the token is removed from storage if we cannot fetch accounts due to a invalid consentId.
     */
    private void removeTokenFromStorageAndThrowSessionError() throws SessionException {
        persistentStorage.remove(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN);
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
