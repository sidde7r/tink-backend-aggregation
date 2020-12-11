package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DemobankAutoAuthenticator implements AutoAuthenticator {
    private final PersistentStorage persistentStorage;
    private final DemobankApiClient apiClient;

    public DemobankAutoAuthenticator(
            PersistentStorage persistentStorage, DemobankApiClient apiClient) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(DemobankConstants.StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                persistentStorage.remove(DemobankConstants.StorageKeys.OAUTH2_TOKEN);
                throw SessionError.SESSION_EXPIRED.exception();
            }

            try {
                oAuth2Token = apiClient.refreshToken(oAuth2Token.getRefreshToken().get());
                apiClient.setTokenToStorage(oAuth2Token);
            } catch (HttpResponseException ex) {
                persistentStorage.remove(DemobankConstants.StorageKeys.OAUTH2_TOKEN);
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
    }
}
