package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderSessionHandler implements SessionHandler {

    private final SantanderApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderSessionHandler(SantanderApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        sessionStorage.clear();
        // API does not support logout
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
