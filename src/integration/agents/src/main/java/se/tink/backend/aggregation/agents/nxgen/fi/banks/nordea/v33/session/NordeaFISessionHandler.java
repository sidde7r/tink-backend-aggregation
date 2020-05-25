package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaFISessionHandler implements SessionHandler {
    private final NordeaFIApiClient apiClient;

    public NordeaFISessionHandler(NordeaFIApiClient apiClient) {
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
