package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SantanderEsSessionHandler implements SessionHandler {
    private final SantanderEsApiClient apiClient;

    public SantanderEsSessionHandler(SantanderEsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.login();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
