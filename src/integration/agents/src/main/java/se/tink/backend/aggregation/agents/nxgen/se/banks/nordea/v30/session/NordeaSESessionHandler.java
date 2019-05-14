package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaSESessionHandler implements SessionHandler {
    private final NordeaSEApiClient apiClient;

    public NordeaSESessionHandler(NordeaSEApiClient apiClient) {
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
