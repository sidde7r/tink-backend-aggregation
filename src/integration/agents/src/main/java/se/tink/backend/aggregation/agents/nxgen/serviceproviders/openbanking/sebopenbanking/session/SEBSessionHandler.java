package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SEBSessionHandler implements SessionHandler {
    private final SebApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SEBSessionHandler(SebApiClient apiClient, SessionStorage sessionStorage) {

        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private void fetchToken() throws SessionException {
        this.sessionStorage
                .get(SebConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .filter(t -> t.isValid())
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {

        fetchToken();
        try {
            this.apiClient.fetchAccounts();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
