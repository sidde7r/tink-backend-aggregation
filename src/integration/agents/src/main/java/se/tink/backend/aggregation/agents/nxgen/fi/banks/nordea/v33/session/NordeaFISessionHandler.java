package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaFISessionHandler implements SessionHandler {
    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaFISessionHandler(NordeaFIApiClient apiClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        apiClient.keepAlive();
    }
}
