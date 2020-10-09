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
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankAutoAuthenticator implements AutoAuthenticator {
    private final SessionStorage sessionStorage;
    private final DemobankApiClient apiClient;

    public DemobankAutoAuthenticator(SessionStorage sessionStorage, DemobankApiClient apiClient) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        OAuth2Token oAuth2Token =
                sessionStorage
                        .get(DemobankConstants.StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                sessionStorage.remove(DemobankConstants.StorageKeys.OAUTH2_TOKEN);
                throw SessionError.SESSION_EXPIRED.exception();
            }

            try {
                oAuth2Token = apiClient.refreshToken(oAuth2Token.getRefreshToken().get());
                apiClient.setTokenToSession(oAuth2Token);
            } catch (HttpResponseException ex) {
                sessionStorage.remove(DemobankConstants.StorageKeys.OAUTH2_TOKEN);
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
    }
}
