package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaSessionHandler implements SessionHandler {
    private final NordeaBaseApiClient apiClient;

    public NordeaSessionHandler(NordeaBaseApiClient apiClient) {
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
