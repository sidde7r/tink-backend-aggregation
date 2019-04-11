package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaSessionHandler implements SessionHandler {

    private final NordeaFiApiClient apiClient;

    public NordeaSessionHandler(NordeaFiApiClient apiClient) {
        this.apiClient = apiClient;
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
